package com.myoung.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoung.seckill.exception.GlobalException;
import com.myoung.seckill.mapper.OrderMapper;
import com.myoung.seckill.pojo.Order;
import com.myoung.seckill.pojo.SeckillGoods;
import com.myoung.seckill.pojo.SeckillOrder;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.IGoodsService;
import com.myoung.seckill.service.IOrderService;
import com.myoung.seckill.service.ISeckillGoodsService;
import com.myoung.seckill.service.ISeckillOrderService;
import com.myoung.seckill.utils.MD5Util;
import com.myoung.seckill.utils.UUIDUtil;
import com.myoung.seckill.vo.GoodsVo;
import com.myoung.seckill.vo.OrderDetailVo;
import com.myoung.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 执行秒杀中，落库的操作
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goods) {

        // mybatis-plus，秒杀商品减库存。为什么会进行两次访库，第一次获取seckillGoods，第二次修改seckillGoods后插入？？
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 判断是否有库存，如果库存为0的话，在redis中设置一个空标记
        if (seckillGoods.getStockCount() < 1){
            valueOperations.set("isStockEmpty:" + goods.getId(), "1");
            return null;
        }
        // 如果不为空的话，就执行减库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        boolean seckillGoodsResult = seckillGoodsService.update( new UpdateWrapper<SeckillGoods>().set("stock_count", seckillGoods.getStockCount()).eq("id",  seckillGoods.getId()).gt("stock_count", 0));

        // 生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setCreateDate(new Date());
        order.setStatus(0);
        order.setOrderChannel(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setGoodsCount(1);
        order.setGoodsName(goods.getGoodName());
        order.setDeliveryAddrId(0L);
        // plus，将订单写入数据库
        orderMapper.insert(order);
        // 这两种写法有什么区别
        // orderService.save(order);

        // 生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        // order.getId() 这个id  应该是需要从数据库读的
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(order.getGoodsId());
        seckillOrder.setUserId(user.getId());
        // mybatis-plus，将秒杀订单写入
        seckillOrderService.save(seckillOrder);

        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goods.getId(), seckillOrder);
        return order;
    }

    /**
     * 获得商品详情
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        // 订单不存在
        if (orderId == null){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setOrder(order);
        detailVo.setGoodsVo(goodsVo);

        return detailVo;
    }

    /**
     * 生成秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPaht:"+ user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 判断秒杀地址是否合法
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0|| StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }
}
