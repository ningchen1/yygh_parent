package com.atguigu.yygh.user.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.prop.WeixinProperties;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/*====================================================
                时间: 2022-06-02
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@Controller
@RequestMapping("/user/userinfo/wx")
public class WeixinController {

    @Autowired
    private WeixinProperties weixinProperties;
    @Autowired
    private UserInfoService userInfoService;


    @GetMapping("/param")
    @ResponseBody
    public R getWeixinLoginParam() throws UnsupportedEncodingException {
        String url = URLEncoder.encode(weixinProperties.getRedirecturl(), "UTF-8");

        Map<String,Object> map=new HashMap<String,Object>();
        map.put("appid",weixinProperties.getAppid());
        map.put("scope","snsapi_login");
        map.put("redirecturl",url);
        map.put("state",System.currentTimeMillis()+"");
        return R.ok().data(map);
    }

    @GetMapping("/callback")
    public String callback(String code,String state) throws Exception {

        StringBuilder stringBuilder=new StringBuilder();
        StringBuilder append = stringBuilder.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String format = String.format(append.toString(), weixinProperties.getAppid(), weixinProperties.getAppsecret(), code);

        String result = HttpClientUtils.get(format);
        System.out.println(result);

        JSONObject jsonObject = JSONObject.parseObject(result);
        //access_token访问微信服务器的一个凭证
        String access_token = jsonObject.getString("access_token");
        //openid是微信扫描用户在微信服务器的唯一标识符
        String openid = jsonObject.getString("openid");

        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
        queryWrapper.eq("openid",openid);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);

        if(userInfo == null) { //首次使用微信登录,把用户的微信信息在表中保存一下
            //给微信服务器发送请求获取当前用户信息
            StringBuilder sb = new StringBuilder();

            StringBuilder append1 = sb.append("https://api.weixin.qq.com/sns/userinfo")
                    .append("?access_token=%s")
                    .append("&openid=%s");

            String format1 = String.format(append1.toString(), access_token, openid);
            String s = HttpClientUtils.get(format1);
            System.out.println(s);
            JSONObject jsonObject1 = JSONObject.parseObject(s);
            String nickname = jsonObject1.getString("nickname");

            userInfo = new UserInfo();
            userInfo.setOpenid(openid);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            userInfoService.save(userInfo);
        }

        //代码
        //验证用户的status
        if(userInfo.getStatus() == 0){
            throw new YyghException(20001,"用户锁定中");
        }

        //6.返回用户信息
        Map<String, String> map = new HashMap<String,String>();

        //检查这个用户手机号是否为空:为空，说明这是首次使用微信登录,强制绑定手机号
        if(StringUtils.isEmpty(userInfo.getPhone())){
            map.put("openid",openid);
        }else{//检查这个用户手机号是否为空:不为空，说明这不是首次微信登录
            map.put("openid","");
        }
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);

        //跳转到前端页面
        return "redirect:http://localhost:3000/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="+URLEncoder.encode(map.get("name"),"utf-8");
    }
}
