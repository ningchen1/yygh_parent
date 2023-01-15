package com.atguigu.yygh.user.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/*====================================================
                时间: 2022-06-02
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@ConfigurationProperties(prefix = "weixin")
@Data
 //@Component+@Value
//@Component+@ConfigurationProperties(prefix = "weixin")
//+@ConfigurationProperties(prefix = "weixin")+@EnableConfigurationProperties(value = WeixinProperties.class)
public class WeixinProperties {


    private String appid;
    private String appsecret;
    private String redirecturl;


}
