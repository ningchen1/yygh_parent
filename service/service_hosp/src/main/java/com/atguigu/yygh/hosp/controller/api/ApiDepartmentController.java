package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.utlis.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/api/hosp")
public class ApiDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //医院的删除
    @PostMapping("/department/remove")
    public Result remove(HttpServletRequest httpServletRequest){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(httpServletRequest.getParameterMap());
        //signkey验证
        departmentService.remove(stringObjectMap);

        return Result.ok();
    }

    //查询科室信息
    @PostMapping("/department/list")
    public Result<Page> getDepartmentPage(HttpServletRequest httpServletRequest){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(httpServletRequest.getParameterMap());
        //1.验证signkey
        Page<Department> page= departmentService.getDepartmentPage(stringObjectMap);
        return Result.ok(page);
    }

    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证：signkey
        departmentService.saveDepartment(stringObjectMap);
        return Result.ok();
    }
}
