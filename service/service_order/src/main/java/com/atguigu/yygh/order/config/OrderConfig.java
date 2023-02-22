package com.atguigu.yygh.order.config;

import com.atguigu.yygh.mq.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration

public class OrderConfig {


    //交换机
    @Bean
    public Exchange getExchange(){
        return ExchangeBuilder.directExchange(MqConst.EXCHANGE_DIRECT_ORDER).durable(true).build();
    }
    //队列
    @Bean
    public Queue getQueue(){
        return QueueBuilder.durable(MqConst.QUEUE_ORDER).build();
    }
    //将队列绑定到交换机上
    @Bean
    public Binding binding(@Qualifier("getQueue") Queue queue,@Qualifier("getExchange") Exchange exchange){
       return BindingBuilder.bind(queue).to(exchange).with(MqConst.ROUTING_ORDER).noargs();
    }
}



