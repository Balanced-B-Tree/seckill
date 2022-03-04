package com.myoung.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoung.seckill.exception.GlobalException;
import com.myoung.seckill.mapper.UserMapper;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.IUserService;
import com.myoung.seckill.utils.CookieUtil;
import com.myoung.seckill.utils.MD5Util;
import com.myoung.seckill.utils.UUIDUtil;
import com.myoung.seckill.vo.LoginVo;
import com.myoung.seckill.vo.RespBean;
import com.myoung.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author jobob
 * @since 2021-08-06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        // 从前端得到的密码，是经过一次加密的
        String password = loginVo.getPassword();

        // 写了校验的注解后，将注解放在 LoginVo的相应参数上，就不用在这里写对参数的校验了
        // 但是，注意，校验失败，如何返回
//        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//
//        // 手机号格式判断
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }

        User user = userMapper.selectById(mobile); // UserMapper.xml里啥都没有，咋执行的selectById
        if (null == user) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 判断密码是否正确
        if (!MD5Util.fromPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        // 生成cookie
        String ticket = UUIDUtil.uuid();
        // 将用户信息存入到redis中
        redisTemplate.opsForValue().set("user"+ticket, user);
        // request.getSession().setAttribute(ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        return RespBean.success();
    }

    // 在redis中获取用户信息
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)){
            return null;
        }
        // 从redis中获取用户信息
        User user = (User) redisTemplate.opsForValue().get("user"+userTicket);
        if (user!= null){
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }


}
