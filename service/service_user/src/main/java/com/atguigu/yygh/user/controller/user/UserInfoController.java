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


    //用户信息更新，用于用户认证
    @PutMapping("/update")
    public R save(@RequestHeader String token, UserAuthVo userAuthVo){
        //使用工具类对token进行解析，获取userid
        Long userId = JwtHelper.getUserId(token);

        //更新用户信息
        UserInfo userInfo=new UserInfo();
        userInfo.setId(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());//证件类型
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());//证件号
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());//证件路径
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());//认证中
        userInfoService.updateById(userInfo);
        return R.ok();

    }

    //用户登录
    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo){
        //用map接收，方便后面获取登录人员的信息
       Map<String,Object> map = userInfoService.login(loginVo);
       return R.ok().data(map);
    }

    //查询用户详情信息
    @GetMapping("/info")
    public R getUserInfo(@RequestHeader String token){
        //使用工具类对token进行解析，获取userid
        Long userId = JwtHelper.getUserId(token);
        UserInfo byId = userInfoService.getUserInfo(userId);

        return R.ok().data("user",byId);
    }



}

