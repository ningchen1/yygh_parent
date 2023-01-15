package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {


    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper=new QueryWrapper<PaymentInfo>();
        queryWrapper.eq("order_id",order.getId());
        PaymentInfo paymentInfo1 = baseMapper.selectOne(queryWrapper);
        if(paymentInfo1 != null){
            return;
        }

        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(order.getAmount());

        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus()); //支付状态
        baseMapper.insert(paymentInfo);
    }
}