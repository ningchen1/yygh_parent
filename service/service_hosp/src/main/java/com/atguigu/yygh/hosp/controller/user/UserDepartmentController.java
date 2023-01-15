package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/hosp/department")
public class UserDepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/all/{hoscode}")
    public R findAll(@PathVariable String hoscode){
        List<DepartmentVo> departmentList=departmentService.getDepartmentList(hoscode);
        return R.ok().data("list",departmentList);
    }
}
