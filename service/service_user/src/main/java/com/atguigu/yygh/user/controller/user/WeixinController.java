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

@Controller
@RequestMapping("/user/userinfo/wx")
public class WeixinController {

    @Autowired
    private WeixinProperties weixinProperties;
    @Autowired
    private UserInfoService userInfoService;


    //获取微信登录参数
    @GetMapping("/param")
    @ResponseBody
    public R getWeixinLoginParam() throws UnsupportedEncodingException {
        //按照微信说明，对微信服务器地址进行重定向编码
        String url = URLEncoder.encode(weixinProperties.getRedirecturl(), "UTF-8");

        Map<String,Object> map=new HashMap<String,Object>();
        map.put("appid",weixinProperties.getAppid());
        map.put("scope","snsapi_login");
        map.put("redirecturl",url);
        map.put("state",System.currentTimeMillis()+"");
        return R.ok().data(map);
    }

    @GetMapping("/callback")
    //回调微信服务器会返回code和state两个参数
    public String callback(String code,String state) throws Exception {

        //第二步 拿着code和微信id和秘钥，请求微信固定地址 ，得到两个值
        //使用code和appid以及appscrect换取access_token
        //  %s   占位符
        StringBuilder stringBuilder=new StringBuilder();
        StringBuilder append = stringBuilder.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String format = String.format(append.toString(), weixinProperties.getAppid(), weixinProperties.getAppsecret(), code);
        //第二步 拿着code和微信id和秘钥，请求微信固定地址 ，得到两个值
        String result = HttpClientUtils.get(format);
        System.out.println(result);
        //把获取到的值进行解析，转化为JSONObject类型
        JSONObject jsonObject = JSONObject.parseObject(result);
        //根据键获取值access_token，access_token访问微信服务器的一个凭证
        String access_token = jsonObject.getString("access_token");
        //根据键获取值openid，openid是微信扫描用户在微信服务器的唯一标识符
        String openid = jsonObject.getString("openid");

        //根据openid进行查询
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
        queryWrapper.eq("openid",openid);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);

        if(userInfo == null) { //首次使用微信登录,把用户的微信信息在表中保存一下
            //给微信服务器发送请求获取当前用户信息
            StringBuilder sb = new StringBuilder();

            StringBuilder append1 = sb.append("https://api.weixin.qq.com/sns/userinfo")
                    .append("?access_token=%s")
                    .append("&openid=%s");
            //对StringBuilder进行拼接
            String format1 = String.format(append1.toString(), access_token, openid);
            //通过HttpClientUtils方法请求微信服务器获当前用户信息
            String s = HttpClientUtils.get(format1);
            System.out.println(s);
            //对获取到的用户信息进行转化为JSONObject格式
            JSONObject jsonObject1 = JSONObject.parseObject(s);
            //获取用户信息中的昵称
            String nickname = jsonObject1.getString("nickname");

            //将获取到的用户信息保存到数据表中
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
        }else{//检查这个用户手机号是否为空:不为空，说明这不是首次微信登录，前端判定不为空则不需要强制绑定手机号
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
