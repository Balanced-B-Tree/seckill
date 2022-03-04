package com.myoung.seckill.rabbitmq;

import com.myoung.seckill.pojo.SeckillMessage;
import com.myoung.seckill.pojo.SeckillOrder;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.impl.GoodsServiceImpl;
import com.myoung.seckill.service.impl.OrderServiceImpl;
import com.myoung.seckill.utils.JsonUtil;
import com.myoung.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RabbitMQReceiver {

    @Autowired
    private GoodsServiceImpl goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderServiceImpl orderService;



    @RabbitListener(queues = "seckillQueue")
    public void receive(String msg) {
        log.info("接收消息 ： " + msg);
        // Json格式的将消息转换为 SeckillMessage对象
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(msg, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        // 库存判断
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        // 重复抢购判断
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null){
            return;
        }
        // 下单
        orderService.seckill(user, goodsVo);


    }
}
