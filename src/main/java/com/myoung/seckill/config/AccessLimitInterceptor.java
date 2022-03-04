package com.myoung.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.IUserService;
import com.myoung.seckill.utils.CookieUtil;
import com.myoung.seckill.vo.RespBean;
import com.myoung.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    @Autowired
    IUserService userService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 在handler执行之前，返回 boolean 值，true 表示继续执行，false 为停止执行并返回。
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            User user = getUser(request, response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            // 获得方法上的注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){  // 如果注解为空，则返回 true继续运行
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin =  accessLimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin){  // 判断是否需要登录
                if (user == null){
                    render(response, RespBeanEnum.ERROR);
                    return false;
                }
                key += ":" + user.getId();
            }
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(key);
            // second秒钟内，最多登录maxCount次
            if (count == null) {
                valueOperations.set(key, 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                valueOperations.increment(key);
            } else {
                render(response, RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    /**
     * 构建返回对象
     * @param response
     * @param error
     */
    private void render(HttpServletResponse response, RespBeanEnum error) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        RespBean respBean = RespBean.error(error);
        out.write(new ObjectMapper().writeValueAsString(respBean));
        out.flush();
        out.close();
    }

    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "usetTicket");
        if (StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookie(ticket, request, response);
    }

    
}
