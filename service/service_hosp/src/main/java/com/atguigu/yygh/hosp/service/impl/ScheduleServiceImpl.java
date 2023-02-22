package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl  implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void saveSchedule(Map<String, Object> stringObjectMap) {
        //将stringObjectMap转化为对象类型，用Schedule接收
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Schedule.class);
        //获取医院编号和科室编号排班编号
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();
        String hosScheduleId = schedule.getHosScheduleId();
        //根据医院编号和科室编号排班id查询排班信息
        Schedule platformSchedule=scheduleRepository.findByHoscodeAndDepcodeAndHosScheduleId(hoscode,depcode,hosScheduleId);

        //若排班信息为空则添加，不为空则修改
        if(platformSchedule == null){
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else{
            schedule.setCreateTime(platformSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(platformSchedule.getIsDeleted());
            schedule.setId(platformSchedule.getId());
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page<Schedule> getSchedulePage(Map<String, Object> stringObjectMap) {
        //创建排班对象
        Schedule schedule=new Schedule();
        //3.获取传递过来的医院编号
        String hoscode = (String)stringObjectMap.get("hoscode");
        //把获取到的医院编号设置到排班信息中
        schedule.setHoscode(hoscode);
        //2.根据排班中的医院编号设置查询条件
        Example<Schedule> scheduleExample=Example.of(schedule);
        //2.1设置分页的条件
        int page = Integer.parseInt(stringObjectMap.get("page").toString());
        int limit = Integer.parseInt(stringObjectMap.get("limit").toString());

        //分页查询根据创建时间进行升序排列
        PageRequest pageRequest = PageRequest.of(page-1, limit, Sort.by("createTime").ascending());
        //1.查询排班信息，根据查询条件和分页scheduleExample，pageRequest
        Page<Schedule> result = scheduleRepository.findAll(scheduleExample, pageRequest);
        return result;
    }

    @Override
    public void remove(Map<String, Object> stringObjectMap) {
        //根据键获取值，医院编号和排班id
       String hoscode =  (String)stringObjectMap.get("hoscode");
       String hosScheduleId =  (String)stringObjectMap.get("hosScheduleId");
        //根据医院编号和排班编号查询排班信息
       Schedule schedule= scheduleRepository.findByHoscodeAndHosScheduleId(hoscode,hosScheduleId);
        //根据查询到的排班信息中的排班id进行删除
       if(schedule != null){
           scheduleRepository.deleteById(schedule.getId());
       }

    }

    //根据医院编号 和 科室编号 ，查询排班规则数据
    @Override
    public Map<String, Object> page(Integer pageNum, Integer pageSize, String hoscode, String depcode) {

        //构造查询条件，判断获取的医院编号和科室编号和数据库中的相等
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //聚合：最好使用mongoTemplate
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//设置分组，根据日期
                        .first("workDate").as("workDate")//根据日期，as是起别名
//                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")//总预约数
                        .sum("availableNumber").as("availableNumber"),//剩余可与约数
                Aggregation.sort(Sort.Direction.ASC, "workDate"),//根据workDate进行升序排列
                Aggregation.skip((pageNum - 1) * pageSize),
                Aggregation.limit(pageSize)
        );//聚合条件
        /**
         * Aggregation:表示聚合条件
         * InputType:表示输入类型，可以根据当前指定的字节码找到mongo对应集合
         * OutputType:表示输出类型，封装聚合后的信息
         */
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //当前页对应的列表数据
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        for (BookingScheduleRuleVo bookingScheduleRuleVo : mappedResults) {
            //获取当前页列表数据的工作日期
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            //工具类：日期转换为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

        }


        //查询分组后的总记录数
        Aggregation aggregation2 = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate"));//聚合条件
        /**
         * Aggregation:表示聚合条件
         * InputType:表示输入类型，可以根据当前指定的字节码找到mongo对应集合
         * OutputType:表示输出类型，封装聚合后的信息
         */
        AggregationResults<BookingScheduleRuleVo> aggregate2 = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);

        Map<String,Object> map=new HashMap<>();
        map.put("list",mappedResults);//当前页列表数据
        map.put("total",aggregate2.getMappedResults().size());//总记录数

        //获取医院名称
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hospital.getHosname());

        map.put("baseMap",baseMap);

        return map;
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @Override
    public List<Schedule> detail(String hoscode, String depcode, String workdate) {
        List<Schedule> scheduleList=scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workdate).toDate());
        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        scheduleList.stream().forEach(item->{
            this.packageSchedule(item);
        });

        return scheduleList;
    }

    //根据医院编号和科室编号查看排班数据，进行分页
    @Override
    public Map<String, Object> getSchedulePageByCondition(String hoscode, String depcode, Integer pageNum, Integer pageSize) {
        //通过医院编号获取医院信息
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if (hospital==null){
            throw new YyghException(20001,"该医院信息不存在");

        }
        //获取预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期分页数据
        IPage<Date> page = this.getListDate(pageNum, pageSize, bookingRule);
        //当前页可预约日期列表
        List<Date> records = page.getRecords();

        //获取可预约日期科室剩余预约数
        //根据医院编号和科室编号和工作日期进行条件查询
        Criteria criteria=Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode).and("workDate").in(records);
        //first为查询字段，相当于 select workDate form 表名 group by workDate ASC
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")//分组
                        .count().as("docCount") //计数
                        .sum("reservedNumber").as("reservedNumber")//总预约数
                        .sum("availableNumber").as("availableNumber"),//剩余可预约数
                Aggregation.sort(Sort.Direction.ASC,"workDate")//根据workDate升序排列
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        //获取数据中的排班列表
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //进行stream流转化为toMap，BookingScheduleRuleVo::getWorkDate为键，BookingScheduleRuleVo -> BookingScheduleRuleVo为值
        Map<Date, BookingScheduleRuleVo> collect = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        int size = records.size();

        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList=new ArrayList<BookingScheduleRuleVo>();
        for(int i=0;i<size;i++){
            Date date = records.get(i);//通过下标去每天的日期
            //根据当天的日期获取聚合后的数据
            BookingScheduleRuleVo bookingScheduleRuleVo = collect.get(date);

            if(bookingScheduleRuleVo == null){
                bookingScheduleRuleVo=new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setWorkDate(date);
                //bookingScheduleRuleVo.setWorkDateMd(date);
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setReservedNumber(0);
                bookingScheduleRuleVo.setAvailableNumber(-1);//当天所有医生的总的剩余可预约数
                //bookingScheduleRuleVo.setStatus(0);
            }


            bookingScheduleRuleVo.setWorkDateMd(date);
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(date)));
            bookingScheduleRuleVo.setStatus(0);

            //第一页第一条做特殊判断处理
            if(i==0 && pageNum == 1){
                DateTime dateTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                //如果医院规定的当前的挂号截止时间在此时此刻之前，说明：此时此刻已经过了当天的挂号截止时间了
                if(dateTime.isBeforeNow()){
                    //当天的挂号截止时间了，则把状态设置为-1
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            //最后一页的最后一条做特殊判断处理
            if(pageNum==page.getPages() && i== (size-1) ){
                bookingScheduleRuleVo.setStatus(1);
            }

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("total",page.getTotal());
        map.put("list",bookingScheduleRuleVoList);

        Map<String,Object> baseMap = new HashMap<String,Object>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospitalByHoscode(hoscode).getHosname());
        //科室
        Department department=departmentService.getDepartment(hoscode,depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());

        map.put("baseMap",baseMap);

        return map;
    }

    //根据排班id获取排班信息
    @Override
    public Schedule getScheduleInfo(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();

        this.packageSchedule(schedule);

        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        ScheduleOrderVo scheduleOrderVo=new ScheduleOrderVo();
        BeanUtils.copyProperties(schedule,scheduleOrderVo);
        Hospital hospital = hospitalService.getHospitalByHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());

        Department department = departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());


        DateTime dateTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(hospital.getBookingRule().getQuitDay()).toDate(), hospital.getBookingRule().getQuitTime());
        scheduleOrderVo.setQuitTime(dateTime.toDate()); //预约的退号截止时间

        Date workDate = schedule.getWorkDate();
        String stopTime = hospital.getBookingRule().getStopTime();
        scheduleOrderVo.setStopTime(this.getDateTime(workDate, stopTime).toDate());

        return scheduleOrderVo;
    }

    //根据排班id查询排班信息更新剩余可预约数
    @Override
    public boolean updateAvailableNumber(String scheduleId, Integer availableNumber) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();

        schedule.setAvailableNumber(availableNumber);
        schedule.setUpdateTime(new Date());

        scheduleRepository.save(schedule);
        return true;
    }

    //取消预约
    @Override
    public void cancelSchedule(String scheduleId) {
        //根据排班id查询排班信息，更新剩余可预约数
        Schedule schedule=scheduleRepository.findByHosScheduleId(scheduleId);
        schedule.setAvailableNumber(schedule.getAvailableNumber()+1);
        scheduleRepository.save(schedule);
    }

    private IPage getListDate(Integer pageNum, Integer pageSize, BookingRule bookingRule) {
//        getCycle预约周期；规定为预约10内的号
        Integer cycle = bookingRule.getCycle();
        //此时此刻是否已经超过了医院规定的当天挂号起始时间，如果此时此刻已经超过了，cycle+1
        //放号时间getReleaseTime
        String releaseTime = bookingRule.getReleaseTime();

        //今天医院规定的挂号起始时间例如，2022-11-26 08:30
        DateTime dateTime = this.getDateTime(new Date(), releaseTime);
        //若今天早上8:30再现在的事件之前，则getCycle预约周期+1天
        if (dateTime.isBeforeNow()){
            cycle=cycle+1;

        }
        //预约周期内所有的时间列表（10/11天）
        List<Date> list=new ArrayList<>();
        for (int i=0;i<cycle;i++){
            String s = new DateTime().plusDays(i).toString("yyyy-MM-dd");

            Date date = new DateTime(s).toDate();
            list.add(date);

        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        //例如从第一页开始，一页展示7天，

        int start = (pageNum-1)*pageSize;

        int end = start+pageSize;

        if(end>list.size()){
            end=list.size();
        }

        //当前页时间列表
        List<Date> currentPageDateList=new ArrayList<Date>();

        for(int j=start;j<end;j++){
            Date date = list.get(j);
            currentPageDateList.add(date);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize, list.size());
        page.setRecords(currentPageDateList);

        return page;


    }
    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    private void packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",
                hospitalService.getHospitalByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }



    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
