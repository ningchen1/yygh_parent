package com.atguigu.yygh.sms.listener;

import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.sms.service.SmsService;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsListener {

    @Autowired
    private SmsService smsService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = MqConst.QUEUE_MSM_SMS),
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_SMS),
                    key = MqConst.ROUTING_SMS_ITEM
            )
    })
    public void consume(MsmVo msmVo, Message message, Channel channel){
        smsService.sendMessage(msmVo);
    }
}
