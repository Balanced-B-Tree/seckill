package com.myoung.seckill.controller;

import com.myoung.seckill.service.IUserService;
import com.myoung.seckill.vo.LoginVo;
import com.myoung.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("/login")
@Controller
@Slf4j  // 写日志的
public class LoginController {

    @Autowired
    IUserService iUserService;

    @RequestMapping("login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        log.info("{}", loginVo);
        return iUserService.doLogin(loginVo, request, response);
    }
}
