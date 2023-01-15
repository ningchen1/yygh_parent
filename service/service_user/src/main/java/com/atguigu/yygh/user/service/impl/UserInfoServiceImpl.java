package com.atguigu.yygh.user.service.impl;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.enums.StatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-06-01
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {


    @Autowired
    private PatientService patientService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.先获取用户输入的手机号和验证码信息
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //2.对接收到的手机号和验证码做一个非空验证
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new  YyghException(20001,"手机号或验证码有误");

        }

        //3.对验证码做进一步确认：
        String redisCode = (String)redisTemplate.opsForValue().get(phone);
        if(StringUtils.isEmpty(redisCode) || !redisCode.equals(code)){
            throw new YyghException(20001,"验证码有误");
        }

        String openid = loginVo.getOpenid();
        UserInfo userInfo=null;
        if(StringUtils.isEmpty(openid)){
            //4.是否手机号首次登录,如果是首次登录，就先往表中注册一下当前用户信息
            QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
            queryWrapper.eq("phone",phone);
            userInfo= baseMapper.selectOne(queryWrapper);
            if(userInfo == null){
                userInfo=new UserInfo();
                userInfo.setPhone(phone);
                baseMapper.insert(userInfo);
                userInfo.setStatus(1);
            }

        }else{//微信强制绑定手机号:首次使用微信登录并且强制绑定手机号的时候会走这个else

            QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
            queryWrapper.eq("openid",openid);
            userInfo = baseMapper.selectOne(queryWrapper);

            QueryWrapper<UserInfo> phoneWrapper=new QueryWrapper<UserInfo>();
            phoneWrapper.eq("phone",phone);
            UserInfo userInfo2= baseMapper.selectOne(phoneWrapper);

            if(userInfo2== null){
                userInfo.setPhone(phone);
                //userInfo.setStatus(1);
                baseMapper.updateById(userInfo);
            }else{
                userInfo2.setOpenid(userInfo.getOpenid());
                userInfo2.setNickName(userInfo.getNickName());
                baseMapper.updateById(userInfo2);
                baseMapper.deleteById(userInfo.getId());
            }
        }
        //5.验证用户的status
        if (userInfo.getStatus() == 0){
            throw new YyghException(20001,"用户锁定中");
        }

        //6.返回用户信息
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        String token = JwtHelper.createToken(userInfo.getId(), name);

        map.put("token", token);
        return map;
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    @Override
    public Page<UserInfo> getUserInfoPage(Integer pageNum, Integer limit, UserInfoQueryVo userInfoQueryVo) {

        Page<UserInfo> page=new Page<>(pageNum,limit);

        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();

        if(!StringUtils.isEmpty(userInfoQueryVo.getKeyword())){
            queryWrapper.like("name",userInfoQueryVo.getKeyword()).or().eq("phone",userInfoQueryVo.getKeyword());
        }
        if(!StringUtils.isEmpty(userInfoQueryVo.getStatus())){
            queryWrapper.eq("status",userInfoQueryVo.getStatus());
        }
        if(!StringUtils.isEmpty(userInfoQueryVo.getAuthStatus())){
            queryWrapper.eq("auth_status",userInfoQueryVo.getAuthStatus());
        }
        if(!StringUtils.isEmpty(userInfoQueryVo.getCreateTimeBegin())){
            queryWrapper.gt("create_time",userInfoQueryVo.getCreateTimeBegin());
        }
        if(!StringUtils.isEmpty(userInfoQueryVo.getCreateTimeEnd())){
            queryWrapper.lt("create_time",userInfoQueryVo.getCreateTimeEnd());
        }

        Page<UserInfo> page1 = baseMapper.selectPage(page, queryWrapper);
        page1.getRecords().stream().forEach(item->{
            this.packageUserInfo(item);
        });

        return page1;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if(status == 0 || status == 1){
            // UserInfo userInfo = baseMapper.selectById(id);
            //mp:支持直接修改的，
            UserInfo userInfo=new UserInfo();
            userInfo.setId(id);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }

    }

    @Override
    public Map<String, Object> detail(Long id) {
        UserInfo userInfo = baseMapper.selectById(id);

        QueryWrapper<Patient> queryWrapper=new QueryWrapper<Patient>();
        queryWrapper.eq("user_id",id);
        List<Patient> patients =patientService.selectList(queryWrapper);

        Map<String, Object> map = new HashMap<String,Object>(2);
        map.put("userInfo",userInfo);
        map.put("patients",patients);
        return map;
    }
    private void packageUserInfo(UserInfo item) {
        Integer authStatus = item.getAuthStatus();
        Integer status = item.getStatus();
        item.getParam().put("statusString", StatusEnum.getStatusStringByStatus(status));
        item.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(authStatus));
    }
}
