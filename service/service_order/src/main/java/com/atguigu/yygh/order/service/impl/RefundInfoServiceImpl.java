package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    //保存退款记录信息
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //根据支付信息获取订单id
        Long orderId = paymentInfo.getOrderId();
        QueryWrapper<RefundInfo> queryWrapper=new QueryWrapper<RefundInfo>();
        queryWrapper.eq("order_id",orderId);
        //根据订单id查询退款记录信息
        RefundInfo refundInfo1 = baseMapper.selectOne(queryWrapper);
        //若不为空则返回信息，防止用户同时打开两个页面进行两条数据的插入
        if(refundInfo1 != null){
            return refundInfo1;
        }
        //若为空则插入一条退款记录信息
        RefundInfo refundInfo=new RefundInfo();
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(PaymentTypeEnum.WEIXIN.getStatus());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setSubject("想退款...");
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
