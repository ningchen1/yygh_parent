package com.atguigu.yygh.user.service;


import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-06-06
 */
public interface PatientService extends IService<Patient> {



    Patient detail(Long id);

    //根据用户id查询其下就诊人信息
    List<Patient> selectList(QueryWrapper<Patient> queryWrapper);

    List<Patient> findAll(String token);
}
