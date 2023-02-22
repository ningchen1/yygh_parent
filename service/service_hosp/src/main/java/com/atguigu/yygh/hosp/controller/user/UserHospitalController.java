package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/hosp/hospital")
public class UserHospitalController {

    @Autowired
    private HospitalService hospitalService;

    //获取所有医院信息
    @GetMapping("/list")
    public R  getHospitalList(HospitalQueryVo hospitalQueryVo){
        Page<Hospital> page = hospitalService.getHospitalPage(1, 1000000, hospitalQueryVo);
        return R.ok().data("list",page.getContent());
    }

    //模糊查询
    @GetMapping("/{name}")
    public R  findByName(@PathVariable String name){
        List<Hospital> list=hospitalService.findByNameLike(name);
        return R.ok().data("list",list);
    }

    //医院详情功能
    @GetMapping("/detail/{hoscode}")
    public R getHospitalDetail(@PathVariable String hoscode){
        Hospital hospital= hospitalService.getHospitalDetail(hoscode);
        return R.ok().data("hospital",hospital);
    }

}
