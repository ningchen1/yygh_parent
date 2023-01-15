package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

//RefundInfoService
public interface RefundInfoService extends IService<RefundInfo> {
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}