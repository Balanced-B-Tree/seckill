package com.myoung.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.vo.LoginVo;
import com.myoung.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author jobob
 * @since 2021-08-06
 */
public interface IUserService extends IService<User> {

    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    // 根据cookie获取用户
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
}
