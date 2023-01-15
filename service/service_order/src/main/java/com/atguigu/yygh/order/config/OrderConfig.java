package com.atguigu.yygh.order.config;

import com.atguigu.yygh.mq.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class OrderConfig {


    @Bean
    public Exchange getExchange(){
        return ExchangeBuilder.directExchange(MqConst.EXCHANGE_DIRECT_ORDER).durable(true).build();
    }
    @Bean
    public Queue getQueue(){
        return QueueBuilder.durable(MqConst.QUEUE_ORDER).build();
    }
    @Bean
    public Binding binding(@Qualifier("getQueue") Queue queue,@Qualifier("getExchange") Exchange exchange){
       return BindingBuilder.bind(queue).to(exchange).with(MqConst.ROUTING_ORDER).noargs();
    }
}



