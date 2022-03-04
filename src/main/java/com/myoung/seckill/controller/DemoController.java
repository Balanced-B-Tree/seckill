package com.myoung.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DemoController {

    @RequestMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "xxxx");

        //  return，表示返回到哪个页面，这里是返回到 hellos页面
        return "hellos";
    }
}
