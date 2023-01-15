package com.atguigu.yygh.hosp.controller.admin;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * 医院设置表 前端控制器
 */
@RestController
@Api(tags = "医院设置信息")
@RequestMapping("/admin/hosp/hospitalSet")
@Slf4j
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;


    //锁定与解锁
    @PutMapping("/status/{id}/{status}")
    public R updateStatus(@PathVariable Long id,@PathVariable Integer status){
        HospitalSet hospitalSet=new HospitalSet();
        hospitalSet.setId(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }
    //批量删除
    @DeleteMapping("/delete")
    public R batchDelete(@RequestBody List<Integer> ids){
        hospitalSetService.removeByIds(ids);
        return R.ok();
    }

    //修改之回显数据
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Integer id){
        return R.ok().data("item",hospitalSetService.getById(id));
    }

    //修改之修改数据
    @PutMapping("/update")
    public R update(@RequestBody HospitalSet hospitalSet){
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    @ApiOperation(value = "新增接口")
    @PostMapping("/save")
    public R save(@RequestBody HospitalSet hospitalSet){
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        //当前时间戳+随机数+MD5加密
        Random random=new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }

    @ApiOperation(value = "带查询条件的分页")
    @PostMapping(value = "/page/{pageNum}/{size}")
    public R getPageInfo(@ApiParam(name = "pageNum",value = "当前页")@PathVariable Integer pageNum,
                         @ApiParam(name = "size",value = "每页显示多少条") @PathVariable Integer size,
                         @RequestBody HospitalSetQueryVo hospitalSetQueryVo){

        Page<HospitalSet> page=new Page<HospitalSet>(pageNum,size);

        QueryWrapper<HospitalSet> queryWrapper=new QueryWrapper<HospitalSet>();
        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())){
            queryWrapper.like("hosname",hospitalSetQueryVo.getHosname());
        }
        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())){
            queryWrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
        }

        hospitalSetService.page(page, queryWrapper);
        return R.ok().data("total",page.getTotal()).data("rows",page.getRecords());

    }

    @ApiOperation(value = "查询所有的医院设置信息")
    @GetMapping(value = "/findAll")
    public R findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("items",list);
    }

    //
    //根据医院设置id删除医院设置信息
    @ApiOperation(value = "根据医院设置id删除医院设置信息")
    @DeleteMapping(value = "/deleteById/{id}")
    public R deleteById(@PathVariable Integer id){
        hospitalSetService.removeById(id);
        return R.ok();
    }

    //string: prefix +string+suffix.html:PC
    //项目 json格式：PC+物手机+联网

    //boolean:


    //List<>
    /*=============================================
          @Api(tags=""):标记在接口类上
          @ApiOperation(value=""):标记在方法上
          @ApiParam(value=""):标记在参数上

          @ApiModel(description=")：对POJO类做说明
          @ApiModelProperty(value=")：对POJO类属性做说明

      ============================================*/
}

