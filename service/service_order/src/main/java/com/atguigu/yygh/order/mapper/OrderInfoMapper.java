package com.atguigu.yygh.order.mapper;


import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    List<OrderCountVo> statistics(OrderCountQueryVo orderCountQueryVo);

}
