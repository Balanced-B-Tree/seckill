package com.myoung.seckill.controller;

import com.myoung.seckill.pojo.User;
import com.myoung.seckill.service.IGoodsService;
import com.myoung.seckill.service.IUserService;
import com.myoung.seckill.vo.DetailVo;
import com.myoung.seckill.vo.GoodsVo;
import com.myoung.seckill.vo.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    IUserService userService;
    @Autowired
    IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    // 根据判断进行路由
    @RequestMapping("/toList")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodList");

        if (!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        // 页面的上下文
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodList", context);
        if (!StringUtils.isEmpty(html)){
            valueOperations.set("goodList", html, 60, TimeUnit.MINUTES);
        }

        return html;
    }

    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId){


        model.addAttribute("user", user);
        GoodsVo goodsVO = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVO.getStartDate();
        Date endDate = goodsVO.getEndDate();
        Date now = new Date();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if (now.before(startDate)){
            seckillStatus = 0;
            remainSeconds = (int) ((startDate.getTime() - now.getTime())/1000);
        }else if (now.after(endDate)){
            seckillStatus = 2;
            remainSeconds = -1;
        }else {
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods", goodsVO);

        DetailVo detailVo = new DetailVo();
        detailVo.setGoodsVo(goodsVO);
        detailVo.setUser(user);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSeckillStatus(seckillStatus);

        return RespBean.success(detailVo);

    }

    @RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user,@PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // Redis中获取页面，如果不为空，则返回缓存的页面
        String html = (String) valueOperations.get("goodsDetail:"+goodsId);
        if (!StringUtils.isEmpty(html)){
            return html;
        }


        model.addAttribute("user", user);
        GoodsVo goodsVO = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVO.getStartDate();
        Date endDate = goodsVO.getEndDate();
        Date now = new Date();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if (now.before(startDate)){
            seckillStatus = 0;
            remainSeconds = (int) ((startDate.getTime() - now.getTime())/1000);
        }else if (now.after(endDate)){
            seckillStatus = 2;
            remainSeconds = -1;
        }else {
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods", goodsVO);

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetails:" + goodsId, html,60, TimeUnit.MINUTES);
        }


        return html;

    }


}
