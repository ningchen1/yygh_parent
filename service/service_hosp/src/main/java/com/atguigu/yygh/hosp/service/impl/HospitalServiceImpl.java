package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


@Service
public class HospitalServiceImpl  implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Autowired
    private DictFeignClient dictFeignClient;


    @Override
    public void saveHospital(Map<String, Object> resultMap) {
        //将resultMap,转化为hospital对象，将键值对类型转化为对象类型
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(resultMap), Hospital.class);
        //获取hospital中得编码hoscode
        String hoscode = hospital.getHoscode();
        //根据获取到的编码查询数据库信息
        Hospital collection=hospitalRepository.findByHoscode(hoscode);
        //若查询到的数据库信息为空，则进行添加
        if(collection == null){//平台上没有该医院信息做添加

            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else{//平台上有该医院信息做修改
            hospital.setStatus(collection.getStatus());
            hospital.setCreateTime(collection.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(collection.getIsDeleted());
            hospital.setId(collection.getId());
            hospitalRepository.save(hospital);
        }


    }

    @Override
    public String getSignKeyWithHoscode(String requestHoscode) {
        //构造查询条件
        QueryWrapper<HospitalSet> hospitalSetQueryWrapper=new QueryWrapper<HospitalSet>();
        hospitalSetQueryWrapper.eq("hoscode", requestHoscode);

        //根据医院编码查询医院信息
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(hospitalSetQueryWrapper);
        if(hospitalSet == null){
           throw new YyghException(20001,"该医院信息不存在");
        }
        //若医院信息存在，则返回医院得签名
        return hospitalSet.getSignKey();
    }

    //通过医院编号获取医院信息
    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        return  hospitalRepository.findByHoscode(hoscode);
    }

    //分页查询医院的所有信息
    @Override
    public Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, HospitalQueryVo hospitalQueryVo) {

        //为下面的Example查询条件构造参数
        Hospital hospital=new Hospital();
        if(!StringUtils.isEmpty(hospitalQueryVo.getHosname())){
            hospital.setHosname(hospitalQueryVo.getHosname());//医院名字
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getProvinceCode())){
            hospital.setProvinceCode(hospitalQueryVo.getProvinceCode());//省编号
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getCityCode())){
            hospital.setCityCode(hospitalQueryVo.getCityCode());//市编号
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getHostype())){
            hospital.setHostype(hospitalQueryVo.getHostype());//医院类型
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getDistrictCode())){
            hospital.setDistrictCode(hospitalQueryVo.getDistrictCode());//区编号
        }
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                //.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        Example<Hospital> of = Example.of(hospital,matcher);

        //分页，根据创建时间进行升序排列
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by("createTime").ascending());

        Page<Hospital> page = hospitalRepository.findAll(of, pageRequest);

        //获取page中的getContent内容，用流的方式进行操作
        page.getContent().stream().forEach(item->{

           this.packageHospital(item);
        });

        return page;
    }

    //根据医院id修改医院状态
    @Override
    public void updateStatus(String id, Integer status) {

        if (status==0 || status==1){
            //先查询再修改
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }


    }

    //根据医院id查询医院的所有信息
    @Override
    public Hospital detail(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        this.packageHospital(hospital);
        return hospital;
    }

    //模糊查询
    @Override
    public List<Hospital> findByNameLike(String name) {

        return hospitalRepository.findByHosnameLike(name);
    }

    //医院详情功能
    @Override
    public Hospital getHospitalDetail(String hoscode) {
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        this.packageHospital(hospital);

        return hospital;
    }

    private void packageHospital(Hospital hospital){
        //医院类型
        String hostype = hospital.getHostype();
        //省编号
        String provinceCode = hospital.getProvinceCode();
        //市编号
        String cityCode = hospital.getCityCode();
        //区编号
        String districtCode = hospital.getDistrictCode();

        //根据省编号查询省名
        String provinceAddress = dictFeignClient.getNameByValue(Long.parseLong(provinceCode));
        //根据市编号查询省名
        String cityAddress = dictFeignClient.getNameByValue(Long.parseLong(cityCode));
        //根据区编号查询省名
        String districtAddress = dictFeignClient.getNameByValue(Long.parseLong(districtCode));
        //根据医院类型和医院等级查询
        String level = dictFeignClient.getNameByDictCodeAndValue(DictEnum.HOSTYPE.getDictCode(), Long.parseLong(hostype));

        hospital.getParam().put("hostypeString", level);
        hospital.getParam().put("fullAddress", provinceAddress+cityAddress+districtAddress + hospital.getAddress());

    }
}
