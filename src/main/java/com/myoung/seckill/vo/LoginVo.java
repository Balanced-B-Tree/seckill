package com.myoung.seckill.vo;

import com.myoung.seckill.validator.IsMobile;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {


    // 校验不通过会抛异常，但是异常只是在console打印，想返回到前端，需要进行特殊处理
    @NotNull  // validation包自带的参数校验
    @IsMobile  // 我们自定义的参数校验
    private String mobile;

    @NotNull
    private String password;
}
