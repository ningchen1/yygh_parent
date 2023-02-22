package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void saveSchedule(Map<String, Object> stringObjectMap);

    Page<Schedule> getSchedulePage(Map<String, Object> stringObjectMap);

    void remove(Map<String, Object> stringObjectMap);

    Map<String, Object> page(Integer pageNum, Integer pageSize, String hoscode, String depcode);

    List<Schedule> detail(String hoscode, String depcode, String workdate);

    Map<String, Object> getSchedulePageByCondition(String hoscode, String depcode, Integer pageNum, Integer pageSize);

    Schedule getScheduleInfo(String scheduleId);

    ScheduleOrderVo getScheduleById(String scheduleId);

    boolean updateAvailableNumber(String scheduleId, Integer availableNumber);

    void cancelSchedule(String scheduleId);
}
