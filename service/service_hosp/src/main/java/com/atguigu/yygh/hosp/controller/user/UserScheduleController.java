package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/hosp/schedule")
public class UserScheduleController {
    @Autowired
    private ScheduleService scheduleService;


    @GetMapping("/{scheduleId}")
    public ScheduleOrderVo getScheduleById(@PathVariable(value = "scheduleId") String scheduleId){
        return scheduleService.getScheduleById(scheduleId);
    }

    //根据排班id获取排班信息
    @GetMapping("/info/{scheduleId}")
    public R getScheduleInfo(@PathVariable String scheduleId){
        Schedule schedule=scheduleService.getScheduleInfo(scheduleId);
        return R.ok().data("schedule",schedule);
    }

    //根据医院编号和科室编号查看排班数据，进行分页
    @GetMapping("/{hoscode}/{depcode}/{pageNum}/{pageSize}")
    public R getSchedulePage(@PathVariable String hoscode,
                             @PathVariable String depcode,
                             @PathVariable Integer pageNum,
                             @PathVariable Integer pageSize){
        //Condition翻译：条件
        Map<String,Object> map=scheduleService.getSchedulePageByCondition(hoscode,depcode,pageNum,pageSize);
        return R.ok().data(map);
    }

    //查询排班详情信息，根据医院编号和科室编号和工作日期
    @GetMapping("/{hoscode}/{depcode}/{workdate}")
    public R getScheduleDetail(@PathVariable String hoscode,
                               @PathVariable String depcode,
                               @PathVariable String workdate){

        List<Schedule> details = scheduleService.detail(hoscode, depcode, workdate);

        return R.ok().data("details",details);
    }
}
