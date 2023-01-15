package com.atguigu.yygh.user.controller.user;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-06-01
 */
@RestController
@RequestMapping("/user/userinfo")
public class UserInfoController {


    @Autowired
    private UserInfoService userInfoService;


    //save
    @PutMapping("/update")
    public R save(@RequestHeader String token, UserAuthVo userAuthVo){
        Long userId = JwtHelper.getUserId(token);

        UserInfo userInfo=new UserInfo();
        userInfo.setId(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        userInfoService.updateById(userInfo);
        return R.ok();

    }

    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo){
       Map<String,Object> map = userInfoService.login(loginVo);
       return R.ok().data(map);
    }

    @GetMapping("/info")
    public R getUserInfo(@RequestHeader String token){
        Long userId = JwtHelper.getUserId(token);
        UserInfo byId = userInfoService.getUserInfo(userId);

        return R.ok().data("user",byId);
    }



}

