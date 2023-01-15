package com.atguigu.yygh.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitService {
   @Autowired
    private RabbitTemplate rabbitTemplate;

   //rabbirmq: String.getBytes()

    //{
    //  key:value1,

    // }
   public boolean sendMessage(String exchange,String routingkey,Object message){
       rabbitTemplate.convertAndSend(exchange,routingkey,message);
       return true;
   }
}
