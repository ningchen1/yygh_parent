package com.atguigu.yygh.user.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
