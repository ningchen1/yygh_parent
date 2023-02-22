package com.atguigu.yygh.user.service.impl;


import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.Action;
import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    //修改就诊人之回显
    @Override
    public Patient detail(Long id) {
        Patient patient = baseMapper.selectById(id);
        this.packagePatient(patient);

        return patient;
    }

    //根据用户id查询其下就诊人信息
    @Override
    public List<Patient> selectList(QueryWrapper<Patient> queryWrapper) {
        List<Patient> patients = baseMapper.selectList(queryWrapper);
        patients.stream().forEach(item->{
            this.packagePatient(item);
        });
        return patients;
    }

    //查询就诊人信息
    @Override
    public List<Patient> findAll(String token) {
        Long userId = JwtHelper.getUserId(token);

        QueryWrapper<Patient> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<Patient> patients = baseMapper.selectList(queryWrapper);

        patients.stream().forEach(item->{
            this.packagePatient(item);
        });

        return patients;
    }

    private void packagePatient(Patient item) {
        //根据获取的证件类型编号，远程调用dictFeignClient，获取证件类型文字
        item.getParam().put("certificatesTypeString",dictFeignClient.getNameByValue(Long.parseLong(item.getCertificatesType())));
        //同理，根据省市区编号获取省市区名称
        String provinceString = dictFeignClient.getNameByValue(Long.parseLong(item.getProvinceCode()));
        String cityString = dictFeignClient.getNameByValue(Long.parseLong(item.getCityCode()));
        String disctrictString = dictFeignClient.getNameByValue(Long.parseLong(item.getDistrictCode()));

        item.getParam().put("provinceString",provinceString);
        item.getParam().put("cityString",cityString);
        item.getParam().put("districtString",disctrictString);
        //保存省市区拼接成的信息
        item.getParam().put("fullAddress",provinceString+cityString+disctrictString+item.getAddress());

    }
}
