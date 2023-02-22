package com.atguigu.yygh.order.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.hosp.client.ScheduleFeignCleint;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.mq.RabbitService;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeiPayService;
import com.atguigu.yygh.order.utils.HttpRequestHelper;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-06-08
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {


    @Autowired
    private ScheduleFeignCleint scheduleFeignCleint;
    @Autowired
    private PatientFeignClient patientFeignClient;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private WeiPayService weiPayService;
    @Autowired
    private PaymentService paymentService;

    //生成订单
    @Override
    public Long submitOrder(String scheduleId, Long patientId) {
        //1.先根据scheduleId获取医生排班信息
        ScheduleOrderVo scheduleById = scheduleFeignCleint.getScheduleById(scheduleId);
        //做判断，若截至时间在排班时间之前则超过了可挂号时间
        if(new DateTime(scheduleById.getStopTime()).isBeforeNow()){
            throw new YyghException(20001,"超过了挂号截止时间");
        }


        //2.先根据patientId获就诊人信息：就诊人id
        Patient patientById = patientFeignClient.getPatientById(patientId);


        //3.从平台请求第三方医院，确认当前用户能否挂号
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",scheduleById.getHoscode());//放入当前排班所在医院编号
        paramMap.put("depcode",scheduleById.getDepcode());//放入当前排班所在科室编号
        paramMap.put("hosScheduleId",scheduleById.getHosScheduleId());//放入当前排班信息所在排班id

        paramMap.put("reserveDate",scheduleById.getReserveDate());//getReserveDate所安排日期
        paramMap.put("reserveTime",scheduleById.getReserveTime());//getReserveTime所安排时间
        paramMap.put("amount",scheduleById.getAmount());//getAmount医师服务费


        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");
        //若返回的信息不为空，且状态码正常，则获取所返回的数据data
        if(jsonObject != null && jsonObject.getInteger("code") == 200){
            JSONObject data = jsonObject.getJSONObject("data");

            //new一个订单对象
            OrderInfo orderInfo=new OrderInfo();
            //将就诊人id，set里订单对象里
            orderInfo.setUserId(patientById.getUserId());

            String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo);//setOutTradeNo交易订单号，由时间戳和随机数生成
            //排班的医院编号
            orderInfo.setHoscode(scheduleById.getHoscode());
            //排班的医院名称
            orderInfo.setHosname(scheduleById.getHosname());
            //排班科室编号
            orderInfo.setDepcode(scheduleById.getDepcode());
            //科室名称
            orderInfo.setDepname(scheduleById.getDepname());
            //医生职称
            orderInfo.setTitle(scheduleById.getTitle());
            //安排日期
            orderInfo.setReserveDate(scheduleById.getReserveDate());
            //安排时间：上午和下午
            orderInfo.setReserveTime(scheduleById.getReserveTime());
            //排班编号（医院自己的排班主键）
            orderInfo.setScheduleId(scheduleById.getHosScheduleId());
            //patient所继承的基本实体的id
            orderInfo.setPatientId(patientById.getId());
            //姓名
            orderInfo.setPatientName(patientById.getName());
            //电话
            orderInfo.setPatientPhone(patientById.getPhone());

            //预约记录唯一标识（医院预约记录主键）
            orderInfo.setHosRecordId(data.getString("hosRecordId"));
            //预约号序
            orderInfo.setNumber(data.getInteger("number"));
            //建议取号时间
            orderInfo.setFetchTime(data.getString("fetchTime"));
            //取号地点
            orderInfo.setFetchAddress(data.getString("fetchAddress"));
            //医师服务费
            orderInfo.setAmount(scheduleById.getAmount());
            //退号时间
            orderInfo.setQuitTime(scheduleById.getQuitTime());
            //订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());

         //3.2 如果返回能挂号，就把取医生排班信息、就诊人信息及第三方医院返回的信息都添加到order_info表中
          baseMapper.insert(orderInfo);
            //3.3 更新平台上对应医生的剩余可预约数
            OrderMqVo orderMqVo=new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            //int reservedNumber = data.getIntValue("reservedNumber");
            int availableNumber = data.getIntValue("availableNumber");
            orderMqVo.setAvailableNumber(availableNumber);

            //3.4 给就诊人发送短信提醒
            MsmVo msmVo=new MsmVo();
            //获取就诊人手机号
            msmVo.setPhone(patientById.getPhone());

            msmVo.setTemplateCode("您已经预约了上午${time}点的${name}医生的号，不要迟到!");
            //setParam模板参数
            Map<String,Object> msmMap=new HashMap<String, Object>();
            msmMap.put("time",scheduleById.getReserveDate()+" "+scheduleById.getReserveTime());
            msmMap.put("name","xxx");
            msmVo.setParam(msmMap);
            orderMqVo.setMsmVo(msmVo);
            //模拟发送短信提示
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);



            //4.返回订单的id
          return orderInfo.getId();

        }else{
            //3.1 如果返回不能挂号，直接抛出异常
            throw new YyghException(20001,"号源已满");
        }
    }

    //带查询条件的订单分页接口
    @Override
    public Page<OrderInfo> getOrderInfoPage(Integer pageNum, Integer pageSize, OrderQueryVo orderQueryVo) {
        Page page=new Page(pageNum,pageSize);
        QueryWrapper<OrderInfo> queryWrapper=new QueryWrapper<OrderInfo>();

        Long userId = orderQueryVo.getUserId(); //用户id
        String outTradeNo = orderQueryVo.getOutTradeNo();//订单号
        String keyword = orderQueryVo.getKeyword();//医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String orderStatus = orderQueryVo.getOrderStatus();//订单状态
        String reserveDate = orderQueryVo.getReserveDate(); //预约日期
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();//下订单时间
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();//下订单时间

        if(!StringUtils.isEmpty(userId)){
            queryWrapper.eq("user_id", userId);
        }
        if(!StringUtils.isEmpty(outTradeNo)){
            queryWrapper.eq("out_trade_no", outTradeNo);
        }
        if(!StringUtils.isEmpty(keyword)){
            queryWrapper.like("hosname", keyword);
        }
        if(!StringUtils.isEmpty(patientId)){
            queryWrapper.eq("patient_id", patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)){
            queryWrapper.eq("order_status", orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)){
            queryWrapper.ge("reserve_date", reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            queryWrapper.le("create_time", createTimeEnd);
        }
        Page<OrderInfo> page1 = baseMapper.selectPage(page, queryWrapper);
        //获取当前页的列表数据转化成流进行遍历
        page1.getRecords().parallelStream().forEach(item->{
            this.packageOrderInfo(item);
        });

        return page1;
    }

    //根据orderId获取订单的详情信息
    @Override
    public OrderInfo detail(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        this.packageOrderInfo(orderInfo);
        return orderInfo;
    }

    //取消挂号订单
    @Override
    public void cancelOrder(Long orderId) {
        //根据订单id查询订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //获取订单信息的退号时间
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        //1.确定当前取消预约的时间 和 挂号订单的取消预约截止时间 对比, 当前时间是否已经超过了 挂号订单的取消预约截止时间
        //1.1 如果超过了，直接抛出异常，不让用户取消
        if(quitTime.isBeforeNow()){
            throw  new YyghException(20001,"超过了退号的截止时间");
        }
        //new医院参数信息
        Map<String,Object>  hospitalParamMap=new HashMap<String,Object>();
        hospitalParamMap.put("hoscode",orderInfo.getHoscode());//医院编号
        hospitalParamMap.put("hosRecordId",orderInfo.getHosRecordId());//预约记录唯一标识（医院预约记录主键）


        //2.从平台请求第三方医院，通知第三方医院，该用户已取消
        JSONObject jsonObject = HttpRequestHelper.sendRequest(hospitalParamMap, "http://localhost:9998/order/updateCancelStatus");
        //2.1 第三方医院如果不同意取消：抛出异常，不能取消
        if(jsonObject == null || jsonObject.getIntValue("code") != 200){
            throw  new YyghException(20001,"取消失败");
        }
        //3.判断用户是否对当前挂号订单是否已支付
        if(orderInfo.getOrderStatus() == OrderStatusEnum.PAID.getStatus()){
            //3.1.如果已支付，退款
            boolean flag= weiPayService.refund(orderId);
            if(!flag){
                throw new YyghException(20001,"退款失败");
            }
        }

        //无论用户是否进了支付

        //4.更新订单的订单状态 及 支付记录表的支付状态
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        baseMapper.updateById(orderInfo);

        UpdateWrapper<PaymentInfo> updateWrapper=new UpdateWrapper<PaymentInfo>();
        updateWrapper.eq("order_id",orderInfo.getId());
        updateWrapper.set("payment_status", PaymentStatusEnum.REFUND.getStatus());
        paymentService.update(updateWrapper);

        //5.更新医生的剩余可预约数信息

        OrderMqVo orderMqVo=new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getScheduleId());//排班id
        MsmVo msmVo=new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        msmVo.setTemplateCode("xxxx.....");
        msmVo.setParam(null);
        orderMqVo.setMsmVo(msmVo);
        //6.给就诊人发送短信提示：
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
    }

    //就医提醒
    @Override
    public void patientRemind() {
        QueryWrapper<OrderInfo> queryWrapper=new QueryWrapper<OrderInfo>();
        //安排日期和订单状态不为-1的
        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        queryWrapper.ne("order_status",-1);

        List<OrderInfo> orderInfos = baseMapper.selectList(queryWrapper);

        for (OrderInfo orderInfo : orderInfos) {

            MsmVo msmVo=new MsmVo();
            //就诊人手机
            msmVo.setPhone(orderInfo.getPatientPhone());

            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);


            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS,MqConst.ROUTING_SMS_ITEM,msmVo);
        }
    }

    //预约统计
    @Override
    public Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo) {
        List<OrderCountVo> countVoList= baseMapper.statistics(orderCountQueryVo);

        //时间列表
        List<String> dateList = countVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //数量列表
        List<Integer> countList = countVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());


        Map<String, Object> map = new HashMap<String,Object>();
        map.put("dateList",dateList);
        map.put("countList",countList);

        return map;
    }
    private void packageOrderInfo(OrderInfo item) {
        //根据看订单状态枚举转化为字
        item.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(item.getOrderStatus()));
    }


}
