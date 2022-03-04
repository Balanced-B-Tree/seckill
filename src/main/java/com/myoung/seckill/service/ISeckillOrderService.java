package com.myoung.seckill.service;

import com.myoung.seckill.pojo.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myoung.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
