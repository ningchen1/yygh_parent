package com.atguigu.yygh.user.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-06-06
 */
//就诊人管理接口
@RestController
@RequestMapping("/user/userinfo/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;
    //增//添加就诊人
    @PostMapping("/save")
    public R save(@RequestBody Patient patient, @RequestHeader String token){
        Long userId = JwtHelper.getUserId(token);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //删
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id){
        patientService.removeById(id);
        return R.ok();
    }
    //修改之回显
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id){
        Patient patient = patientService.detail(id);
        return R.ok().data("patient",patient);
    }
    //修改之更新数据
    @PutMapping("/update")
    public R update(@RequestBody Patient patient){
        patientService.updateById(patient);
        return R.ok();
    }

    //查询详情信息
    @GetMapping("/all")
    public R findAll(@RequestHeader String token){


        List<Patient> list = patientService.findAll(token);

        return R.ok().data("list",list);
    }
    //根据就诊人id获取就诊人信息
    @GetMapping("/{patientId}")
    public Patient getPatientById(@PathVariable("patientId") Long patientId){
        return patientService.getById(patientId);
    }

}
