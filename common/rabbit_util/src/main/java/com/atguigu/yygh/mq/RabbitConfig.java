package com.atguigu.yygh.mq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;


@SpringBootConfiguration
public class RabbitConfig {

    //作用：就是将发送到RabbitMQ中的pojo对象自动就行转换为json格式存储
      //   从rabbitmq中消费消息时，自动把json格式的字符串转换为pojo对象

    @Bean
    public MessageConverter getMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
