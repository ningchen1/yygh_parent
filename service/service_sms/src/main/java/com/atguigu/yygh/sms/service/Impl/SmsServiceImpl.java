package com.atguigu.yygh.sms.service.Impl;

import com.atguigu.yygh.sms.service.SmsService;
import com.atguigu.yygh.sms.utils.RandomUtil;
import com.atguigu.yygh.sms.utils.SMSUtils;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean sendCode(String phone) {


        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String fourBitRandom = RandomUtil.getFourBitRandom();

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("阿里云短信测试", "SMS_154950909", phone, fourBitRandom);
            //需要将生成验证码保存到session

            System.out.println("验证码为"+fourBitRandom);

            //session.setAttribute(phone, code);

            //将生成得验证码缓存到redis中，并且设置有效期5分钟
            redisTemplate.opsForValue().set(phone,fourBitRandom,5, TimeUnit.MINUTES);

            return true;
        }
        return false;
    }

    @Override
    public void sendMessage(MsmVo msmVo) {
        String phone = msmVo.getPhone();
        //阿里云发送短信提示：个人用户
        //模板 //模板参数
        System.out.println("给就诊人发送短信提示成功");
    }
}
