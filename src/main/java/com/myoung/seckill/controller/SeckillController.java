package com.myoung.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myoung.seckill.config.AccessLimit;
import com.myoung.seckill.pojo.Order;
import com.myoung.seckill.pojo.SeckillMessage;
import com.myoung.seckill.pojo.SeckillOrder;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.rabbitmq.RabbitMQSender;
import com.myoung.seckill.service.IGoodsService;
import com.myoung.seckill.service.IOrderService;
import com.myoung.seckill.service.ISeckillOrderService;
import com.myoung.seckill.utils.JsonUtil;
import com.myoung.seckill.vo.GoodsVo;
import com.myoung.seckill.vo.RespBean;
import com.myoung.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/secKill")
public class SeckillController implements InitializingBean {

    @Autowired
    IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    IOrderService orderService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private RabbitMQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;


    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    /**
     * 秒杀controller
     * 第三次优化，使用了秒杀接口隐藏
     * @param path
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
        // 判断用户是否为空，是否登录
        if (user == null) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        // 校验秒杀地址是否正确
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // 判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        // 内存标记，减少redis的访问
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_SOCKET);
        }
        // 预减库存，调用lua脚本，让它称为原子性操作
        ValueOperations valueOperations = redisTemplate.opsForValue();
//        Long stock =  valueOperations.decrement("seckillGoods:" + goodsId); // 原子操作
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_SOCKET);
        }
        // 下单，rabbitMQ
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        // 这里，MQ消息发送成功，返回前端
        return RespBean.success(0);


    }

    /**
     * 获取秒杀结果
     *
     * @param user
     * @param goodsId
     * @return orderId：成功，0：排队中，-1：失败
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 预先把库存信息读到redis中
        List<GoodsVo> goodsVos = goodsService.findGoodsVo();
        if (goodsVos == null) {
            return;
        }
        goodsVos.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("sekillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }

    /**
     * 获得秒杀地址
     *
     * 这边应该再加一个对于时间的限制，根据 goodsID，得到秒杀时间，这个秒杀时间应该存在redis中。
     * 1. 从 redis 中获取秒杀时间，时间不通过，则返回 error；通过则继续
     * 2. 如果 redis 中没有秒杀时间，则从数据库中获取，得到后再存储到 redis 中
     *
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second=5, maxCount=10, needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        // 开始进行接口限流的操作
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String uri = request.getRequestURI();
        // 表示访问了多少次
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    // 优化一次，页面静态化后
    @RequestMapping("/doSecKill")
    @ResponseBody
    public RespBean doSeckill1(Model model, User user, Long goodsId) {
        // 判断用户是否为空，是否登录
        if (user == null) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        model.addAttribute("user", user);
        // 判断库存
        GoodsVo goodsVO = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVO.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_SOCKET.getMessage());
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        // 判断是否重复抢购  mybatis-plus 的写法
        // 在SeckillOrder 秒杀订单中，找 userid、goodsId相等的订单
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));

        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        // 进行秒杀订单的生成
        Order order = orderService.seckill(user, goodsVO);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVO);
        return RespBean.success(order);


    }

    // 没有优化，页面静态化前
    @RequestMapping("/doSecKill")
    public String doSeckill2(Model model, User user, Long goodsId) {
        // 判断用户是否为空，是否登录
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goodsVO = goodsService.findGoodsVoByGoodsId(goodsId);
        // 判断库存
        if (goodsVO.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_SOCKET.getMessage());
            return "secKillFail";
        }
        // 判断是否重复抢购  mybatis-plus 的写法
        // 在SeckillOrder 秒杀订单中，找 userid、goodsId相等的订单
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        // 进行秒杀订单的生成
        Order order = orderService.seckill(user, goodsVO);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVO);
        return "orderDetail";


    }


}
