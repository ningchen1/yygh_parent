package com.atguigu.yygh.statistic.service;

import com.atguigu.yygh.order.client.OrderInfoFeignClient;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class StatisticsService {
    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    public Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo) {
       return orderInfoFeignClient.statistics(orderCountQueryVo);
    }
}
