package com.myoung.seckill.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigDirect {

    // 队列
    private static final String QUEUE_DIRECT01 = "queue_direct01";
    private static final String QUEUE_DIRECT02 = "queue_direct02";

    // 交换机
    private static final String EXCHANGE = "directExchange";

    // 路由键
    private static final String ROUTINGKEY01 = "queue.red";
    private static final String ROUTINGKEY02 = "queue.green";

    // 产生Queue
    @Bean
    public Queue queue01() {
        return new Queue(QUEUE_DIRECT01);
    }
    @Bean
    public Queue queue02() {
        return new Queue(QUEUE_DIRECT02);
    }

    // 产生交换机
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(EXCHANGE);
    }

    // 将队列、交换机、路由键进行绑定
    @Bean
    public Binding bindingQUEUE_DIRECT01(){
        return BindingBuilder.bind(queue01()).to(directExchange()).with(ROUTINGKEY01);
    }
    @Bean
    public Binding binding02(){
        return BindingBuilder.bind(queue02()).to(directExchange()).with(ROUTINGKEY02);
    }



}
