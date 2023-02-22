package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utlis.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiScheduleController {

    @Autowired
    private ScheduleService scheduleService;


    //删除排班
    @PostMapping("/schedule/remove")
    public Result remove(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证sign
        scheduleService.remove(stringObjectMap);
        return Result.ok();
    }
    //查询排班分页信息
    @PostMapping("/schedule/list")
    public Result<Page> getSchedulePage(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证sign
        Page<Schedule> schedulePage=scheduleService.getSchedulePage(stringObjectMap);
        return Result.ok(schedulePage);
    }

    //上传排班信息
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证sign
        scheduleService.saveSchedule(stringObjectMap);
        return Result.ok();
    }
}
