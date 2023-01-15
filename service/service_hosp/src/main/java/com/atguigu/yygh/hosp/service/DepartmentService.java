package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/*************************************************
 时间: 2022-05-27
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
public interface DepartmentService {
    void saveDepartment(Map<String, Object> stringObjectMap);

    Page<Department> getDepartmentPage(Map<String, Object> stringObjectMap);

    void remove(Map<String, Object> stringObjectMap);

    List<DepartmentVo> getDepartmentList(String hoscode);

    String getDepName(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}
