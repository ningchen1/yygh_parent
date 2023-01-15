package com.atguigu.yygh.oss.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "oss.file")
@PropertySource(value = {"classpath:oss.properties"}) //1.@PropertySource不支持yml文件 2.@PropertySource不能和@EnableConfigurationProperties搭配使用
@Data
@Component
public class OssProperties {

    private String endpoint;
    private String keyid;
    private String keysecret;
    private String bucketname;

}
