package com.atguigu.yygh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*************************************************
 时间: 2022-05-28
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
@FeignClient(value = "service-cmn") //被调用方在注册中心的应用名称
public interface DictFeignClient {


    //根据医院所属的省市区编号获取省市区文字
    @GetMapping("/admin/cmn/{value}")
    public String getNameByValue(@PathVariable("value") Long value);

    //根据医院的等级编号获取医院等级信息
    @GetMapping("/admin/cmn/{dictCode}/{value}")
    public String getNameByDictCodeAndValue(@PathVariable("dictCode") String dictCode,@PathVariable("value") Long value);

}
