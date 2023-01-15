package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.acl.User;
import com.atguigu.yygh.vo.acl.UserQueryVo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/admin/user")
public class UserController {


    @PostMapping(value = "/login")
    public R login(@RequestBody User user){
     //暂时不去数据库查:用户系统再去
        return R.ok().data("token","admin-token");
    }

    @GetMapping(value = "/info")
    public R info(String token){
        return R.ok().data("roles","[admin]")
                     .data("introduction","I am a super administrator")
                     .data("avatar","https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.soogif.com%2F2yuV7GrH13xwmAy0Hc04REIWTQUwPEtW.gif&refer=http%3A%2F%2Fimg.soogif.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1655709239&t=1b2b2ba8571e6ae564c9febcf0600f3b")
                     .data("name","Super Admin");
    }


}
