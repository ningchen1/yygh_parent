package com.atguigu.yygh.order.service;

import java.util.Map;

public interface WeiPayService {
    String createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    void paySuccess(Long orderId, Map<String,String> map);

    boolean refund(Long orderId);
}
