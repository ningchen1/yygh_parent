package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.utlis.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiHospitalController {

    @Autowired
    private HospitalService hospitalService;


    //查询医院信息
    @PostMapping("/hospital/show")
    public Result<Hospital> getHospitalInfo(HttpServletRequest request){
        //将getParameterMap转化为map类型
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //获取医院编码
        String hoscode = (String)stringObjectMap.get("hoscode");
        //1.根据医院编码查询医院信息
        Hospital hospital=hospitalService.getHospitalByHoscode(hoscode);
        return Result.ok(hospital);
    }

    //添加医院信息
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request){
        //1.获取所有的参数
        Map<String, Object> resultMap= HttpRequestHelper.switchMap(request.getParameterMap());
        //获取转递过来得医院编码
        String requestHoscode = (String)resultMap.get("hoscode");
        //1.1获取医院系统传递过来得签名，签名已进行MD5加密
        String requestSignKey = (String)resultMap.get("sign");
        //根据医院编码查询医院得签名
        String platformSignKey= hospitalService.getSignKeyWithHoscode(requestHoscode);
        //把从数据库中查询到的签名进行MD5加密
        String encrypt = MD5.encrypt(platformSignKey);

        //signkey验证
        //requestSignKey不能为空，encrypt不能为空，且两者内容一样
        if(!StringUtils.isEmpty(requestSignKey) && !StringUtils.isEmpty(encrypt) && encrypt.equals(requestSignKey) ){

            //传输过程中“+”转换为了“ ”，因此我们要转换回来
            //获取传递过来得信息
            String logoData = (String)resultMap.get("logoData");
            String result = logoData.replaceAll(" ", "+");
            resultMap.put("logoData",result);
            //把resultMap进行保存操作
            hospitalService.saveHospital(resultMap);
            return Result.ok();
        }else{

            throw  new YyghException(20001,"保存失败");
        }
    }
}
