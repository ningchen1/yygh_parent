package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.prop.WeiPayProperties;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeiPayService;
import com.atguigu.yygh.order.utils.HttpClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeiPayServiceImpl implements WeiPayService {

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WeiPayProperties weiPayProperties;
    @Autowired
    private RefundInfoService refundInfoService;
    @Override
    public String createNative(Long orderId) {
        //1.根据订单id去数据库中获取订单信息
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        //2.保存支付记录信息
        paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
        //3.请求微信服务器获取微信支付的url地址
        HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        Map<String,String> paramMap=new HashMap<String,String>();

        paramMap.put("appid",weiPayProperties.getAppid());
        paramMap.put("mch_id",weiPayProperties.getPartner());
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());

        Date reserveDate = orderInfo.getReserveDate();
        String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
        String body = reserveDateString + "就诊"+ orderInfo.getDepname();

        paramMap.put("body",body);
        paramMap.put("out_trade_no",orderInfo.getOutTradeNo());
        paramMap.put("total_fee","1");//支付金額，測試為1分

        paramMap.put("spbill_create_ip","127.0.0.1");
        paramMap.put("notify_url","http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type","NATIVE");



        try{
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey()));//设置超参数
            httpClient.setHttps(true);//支持https协议
            httpClient.post(); //发送请求

            String xmlResult = httpClient.getContent();
            Map<String, String> stringStringMap = WXPayUtil.xmlToMap(xmlResult);
            return stringStringMap.get("code_url");
        }catch (Exception ex){
              return "";
        }

    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId) {


        OrderInfo orderInfo = orderInfoService.getById(orderId);

        HttpClient httpClient =new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        Map<String,String> map = new HashMap<String,String>();
        map.put("appid", weiPayProperties.getAppid());
        map.put("mch_id", weiPayProperties.getPartner());
        map.put("out_trade_no", orderInfo.getOutTradeNo());//商户订单号
        map.put("nonce_str",WXPayUtil.generateNonceStr());


        try{
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(map, weiPayProperties.getPartnerkey()));
            httpClient.setHttps(true);
            httpClient.post();
            String content = httpClient.getContent();
            Map<String, String> stringStringMap = WXPayUtil.xmlToMap(content);

            return stringStringMap; //支付

        } catch (Exception ex){
             return  null;
        }
    }

    @Transactional
    @Override
    public void paySuccess(Long orderId, Map<String,String> map) {
        //更新订单表的订单状态
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        //更新支付记录表的支付状态
        UpdateWrapper updateWrapper=new UpdateWrapper();
        updateWrapper.eq("order_id",orderId);
        updateWrapper.set("trade_no",map.get("transaction_id")); //微信支付的订单号[微信服务器]
        updateWrapper.set("payment_status", PaymentStatusEnum.PAID.getStatus());
        updateWrapper.set("callback_time",new Date());

        updateWrapper.set("callback_content", JSONObject.toJSONString(map));
        paymentService.update(updateWrapper);
    }

    @Override
    public boolean refund(Long orderId) {

        QueryWrapper<PaymentInfo> queryWrapper=new QueryWrapper<PaymentInfo>();
        queryWrapper.eq("order_id",orderId);
        PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
        RefundInfo refundInfo=refundInfoService.saveRefundInfo(paymentInfo);
        //已退款
        if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()){
            return true;
        }
        //执行微信退款
        HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");

        Map<String,String> paramMap = new HashMap<>(8);

        paramMap.put("appid",weiPayProperties.getAppid());       //公众账号ID
        paramMap.put("mch_id",weiPayProperties.getPartner());   //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信支付订单号

        paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
        //       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
        //       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");

        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap,weiPayProperties.getPartnerkey());
            httpClient.setXmlParam(paramXml);
            httpClient.setHttps(true);
            httpClient.setCert(true); //设置证书支持
            httpClient.setCertPassword(weiPayProperties.getPartner()); //设置证书密码
            httpClient.post();

            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            if("SUCCESS".equals(resultMap.get("result_code"))){ //微信退款成功
                refundInfo.setTradeNo(resultMap.get("refund_id"));//微信退款交易号
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackTime(new Date());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;

            }
            return  false;

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }
}
