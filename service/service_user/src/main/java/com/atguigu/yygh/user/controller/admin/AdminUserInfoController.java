package com.atguigu.yygh.user.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/administrator/userinfo")
public class AdminUserInfoController {


    @Autowired
    private UserInfoService userInfoService;

    //修改用户认证状态
    @PutMapping("/auth/{id}/{authStatus}")
    public R approval(@PathVariable Long id,
                          @PathVariable Integer authStatus){
        //0未认证，1认证中，2认证通过，-1认证不通过
        if(authStatus == 2 || authStatus == -1){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAuthStatus(authStatus);
            userInfoService.updateById(userInfo);
        }

        return R.ok();
    }


    //查询用户详情信息，包括下面的就诊人信息
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id){
        Map<String,Object> map= userInfoService.detail(id);
        return R.ok().data(map);
    }

    //修改用户状态
    @PutMapping("/{id}/{status}")
    public R updateStatus(@PathVariable Long id,
                          @PathVariable Integer status){

        userInfoService.updateStatus(id,status);
        return R.ok();
    }



    //管理系统下的就诊人分页查询
    @GetMapping("/{pageNum}/{limit}")
    //用的是GetMapping，所以UserInfoQueryVo不用加@RequestBody
    public R getUserInfoPage(@PathVariable Integer pageNum,
                             @PathVariable Integer limit,
                             UserInfoQueryVo userInfoQueryVo){

        Page<UserInfo> page=userInfoService.getUserInfoPage(pageNum,limit,userInfoQueryVo);
        return R.ok().data("total",page.getTotal()).data("list",page.getRecords());
    }
}
