package com.atguigu.yygh.order.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author atguigu
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {


    @Autowired
    private OrderInfoService orderInfoService;

    //预约统计接口
    @PostMapping("/statistics")
    public Map<String,Object> statistics(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderInfoService.statistics(orderCountQueryVo);
    }

    //取消挂号订单
    @GetMapping("/cancel/{orderId}")
    public R cancelOrder(@PathVariable Long orderId){
        orderInfoService.cancelOrder(orderId);
        return R.ok();
    }

    //根据orderId获取订单的详情信息
    @GetMapping("/{orderId}")
    public R detail(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.detail(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    //获取订单状态
    @GetMapping("/list")
    public R getOrderList(){
        List<Map<String, Object>> statusList = OrderStatusEnum.getStatusList();
        return R.ok().data("list",statusList);
    }


    //带查询条件的订单分页接口
    @GetMapping("/{pageNum}/{pageSize}")
    public R getOrderInfoPage(@PathVariable Integer pageNum,
                              @PathVariable Integer pageSize,
                              OrderQueryVo orderQueryVo,
                              @RequestHeader String token){
        //根据token获取userId
        Long userId = JwtHelper.getUserId(token);
        orderQueryVo.setUserId(userId);
        Page<OrderInfo>  page= orderInfoService.getOrderInfoPage(pageNum,pageSize,orderQueryVo);


        return R.ok().data("page",page);
    }


    //生成预约订单根据排班编号和就诊人id
    @PostMapping("/{scheduleId}/{patientId}")
    public R  submitOrder(@PathVariable String scheduleId,
                          @PathVariable Long patientId){
       Long orderId= orderInfoService.submitOrder(scheduleId,patientId);

       return R.ok().data("orderId",orderId);
    }

}

