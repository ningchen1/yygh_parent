package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.order.service.WeiPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/order/weixin")
public class WeiPayController {

    @Autowired
    private WeiPayService weiPayService;

    @GetMapping("/status/{orderId}")
    public R getPayStatus(@PathVariable Long orderId){
        //根据订单id查询订单状态
        Map<String,String> map=weiPayService.queryPayStatus(orderId);
        if(map == null){
            return R.error().message("查询失败");
        }
        //查询成功+支付成功
        if("SUCCESS".equals(map.get("trade_state"))){ //支付成功
            weiPayService.paySuccess(orderId,map);//更新了订单状态0 1 +支付记录表的支付状态:1 2
            return R.ok();
        }
        //
        return R.ok().message("支付中"); //支付失败
    }


    //获取支付二维码
    @GetMapping("/{orderId}")
    public R createNative(@PathVariable Long orderId){
        String url=  weiPayService.createNative(orderId);
        return R.ok().data("url",url);
    }

}
