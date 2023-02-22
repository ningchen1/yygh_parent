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


//    保存交易记录
    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper=new QueryWrapper<PaymentInfo>();
        //根据订单id查询数据库中的信息
        queryWrapper.eq("order_id",order.getId());
        PaymentInfo paymentInfo1 = baseMapper.selectOne(queryWrapper);
        if(paymentInfo1 != null){
            return;
        }

        PaymentInfo paymentInfo=new PaymentInfo();

        paymentInfo.setOutTradeNo(order.getOutTradeNo());//订单交易号
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);//支付类型
        paymentInfo.setTotalAmount(order.getAmount());//医师服务费

        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);//交易内容
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus()); //支付状态
        baseMapper.insert(paymentInfo);
    }
}