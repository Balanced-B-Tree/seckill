package com.myoung.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoung.seckill.mapper.SeckillOrderMapper;
import com.myoung.seckill.pojo.SeckillOrder;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {


    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return 成功，0：排队中，-1：失败
     */
    @Override
    public Long getResult(User user, Long goodsId) {
        // 读数据库
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        /**
         * 如果不为null，表示秒杀成功
         * 如果为null，有两种可能：
         * 1. 库存不足，如何去查看库存是否不足，用到redis，在判断库存不足时，进行设置
         * 2. 还在排队
         */
        if (null != seckillOrder){
            //
            return seckillOrder.getOrderId();
        }else if (redisTemplate.hasKey("isScoketEmpty:" + goodsId)){
            return 1L;
        }else {
            return 0L;
        }

    }
}
