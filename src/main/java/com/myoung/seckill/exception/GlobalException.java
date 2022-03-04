package com.myoung.seckill.exception;


import com.myoung.seckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {

    // 这个RespBeanEnum 是如何进行赋值的
    private RespBeanEnum respBeanEnum;
}
