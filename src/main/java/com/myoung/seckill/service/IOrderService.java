package com.myoung.seckill.service;

import com.myoung.seckill.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.vo.GoodsVo;
import com.myoung.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goods);

    OrderDetailVo detail(Long orderId);

    String createPath(User user, Long goodsId);

    boolean checkPath(User user, Long goodsId, String path);
}
