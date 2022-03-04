package com.myoung.seckill.controller;


import com.myoung.seckill.pojo.User;
import com.myoung.seckill.rabbitmq.MQSender;
import com.myoung.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author jobob
 * @since 2021-08-06
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    MQSender sender;

    @RequestMapping("info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq() {
        sender.send("hello");
    }

    @RequestMapping("/mq/direct01")
    @ResponseBody
    public void mq01() {
        sender.send01("hello,red");
    }

    @RequestMapping("/mq/direct02")
    @ResponseBody
    public void mq02() {
        sender.send02("hello,green");
    }

    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void mq03() {
        sender.send03("hello,red");
    }

    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void mq04() {
        sender.send04("hello,green");
    }

}
