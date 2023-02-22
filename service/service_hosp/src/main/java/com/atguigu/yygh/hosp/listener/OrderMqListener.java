package com.atguigu.yygh.hosp.listener;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.mq.RabbitService;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderMqListener {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value =@Queue(name = MqConst.QUEUE_ORDER,durable = "true"),//创建队列
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER), //创建交换机
                    key=MqConst.ROUTING_ORDER
            )
    })

    //确认挂号：走该方法 -n
    //取消预约：走方法 : +1
    public void consume(OrderMqVo orderMqVo, Message message, Channel channel){
        String scheduleId = orderMqVo.getScheduleId();//获取订单信息中的排班id
        Integer availableNumber = orderMqVo.getAvailableNumber();//剩余预约数
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(availableNumber != null){
            //根据排班id查询排班信息更新剩余可预约数
            boolean flag= scheduleService.updateAvailableNumber(scheduleId,availableNumber);

        }else{
            //取消预约
            scheduleService.cancelSchedule(scheduleId);
        }

        if(msmVo != null){
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS,MqConst.ROUTING_SMS_ITEM,msmVo);
        }


    }
}
