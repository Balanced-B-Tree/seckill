package com.myoung.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigTopic {
    // 队列
    private static final String QUEUE01 = "queue_topic01";
    private static final String QUEUE02 = "queue_topic02";

    // 交换机
    private static final String EXCHANGE = "topicExchange";

    // 路由键
    private static final String ROUTINGKEY01 = "#.queue.#";
    private static final String ROUTINGKEY02 = "*.queue.#";

    // 创建队列
    @Bean
    public Queue queue01() {
        return new Queue(QUEUE01);
    }
    @Bean
    public Queue queue02() {
        return new Queue(QUEUE02);
    }

    // 创建交换机
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // 将队列、交换机、路由键进行绑定
    @Bean
    public Binding binding01() {
        return BindingBuilder.bind(queue01()).to(topicExchange()).with(ROUTINGKEY01);
    }
    @Bean
    public Binding binding02() {
        return BindingBuilder.bind(queue01()).to(topicExchange()).with(ROUTINGKEY02);
    }

}
