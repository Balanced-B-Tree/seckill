package com.myoung.seckill.controller;


import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.IOrderService;
import com.myoung.seckill.vo.OrderDetailVo;
import com.myoung.seckill.vo.RespBean;
import com.myoung.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId){
        if (user == null){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        OrderDetailVo orderDetailVo = orderService.detail(orderId);
        return RespBean.success(orderDetailVo);
    }

}
