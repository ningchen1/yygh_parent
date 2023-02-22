package com.atguigu.yygh.cmn.service.impl;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-05-23
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    /*=============================================
         springcache:底层redis、memcache
           1. 导入starter依赖
           2. application.properties: redis连接信息
           3. 在配置类中提供一个cacheManager,在配置类上标记@EnableCaching开启缓存支持注解
           4.@Cacheable(value="")            ：key::value
      ============================================*/

    //@Cacheable(value = "dict",keyGenerator = "keyGenerator")

    //根据数据id查询子数据列表
    @Override
    //,key = "'selectIndexList'+#pid"
   @Cacheable(value = "abc",key = "'selectIndexList'+#pid")
    public List<Dict> getChildListByPid(Long pid) {
        //构造查询条件
        QueryWrapper<Dict> queryWrapper=new QueryWrapper<Dict>();
        queryWrapper.eq("parent_id",pid);

        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        for (Dict dict : dicts) {
            //isHasChildren方法，是判断该id下有没有子元素，有的话返回true,没有返回false，用来显示父id左侧的箭头
             dict.setHasChildren(isHasChildren(dict.getId()));
        }
        return dicts;
    }

    @Override
    public void download(HttpServletResponse response) throws IOException {
        List<Dict> list =baseMapper.selectList(null);
        List<DictEeVo> dictEeVoList = new ArrayList<DictEeVo>(list.size());
        for (Dict dict : list) {
            DictEeVo dictEeVo=new DictEeVo();
            BeanUtils.copyProperties(dict,dictEeVo);//要求源对象dict和目标对象dictEeVo对应的属性名必须相同
            dictEeVoList.add(dictEeVo);
        }
        //下载：响应头信息
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("字典文件", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("学生列表1").doWrite(dictEeVoList);
    }

//@Cacheable:用在查询方法上，表示查询数据的时候，先去缓存中查询，缓存中没有再去数据库中查询
//@CacheEvict：删除、修改方法上，表示当修改删除数据表中的数据，把缓存中对应的数据删除掉
    @Override
    @CacheEvict(value = "abc", allEntries=true)
    public void upload(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet(0).doRead();
    }

    //根据医院所属的省市区编号获取省市区文字
    @Override
    public String getNameByValue(Long value) {
        QueryWrapper<Dict> queryWrapper=new QueryWrapper<Dict>();
        queryWrapper.eq("value",value);
        //根据医院所属的省市区编号获取省市区文字
        Dict dict = baseMapper.selectOne(queryWrapper);
        if(dict != null){
            return dict.getName();
        }
        return null;
    }

    //根据医院的等级编号获取医院等级信息
    @Override
    public String getNameByDictCodeAndValue(String dictCode,Long value) {
        //根据字典编码获取相应的字典信息
        QueryWrapper<Dict> queryWrapper=new QueryWrapper<Dict>();
        queryWrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);

        //根据字典信息id和value查询医院等级信息名称
        QueryWrapper<Dict> queryWrapper2=new QueryWrapper<Dict>();
        queryWrapper2.eq("parent_id", dict.getId());
        queryWrapper2.eq("value", value);

        Dict dict2 = baseMapper.selectOne(queryWrapper2);

        return dict2.getName();
    }


    //根据数据id查询子数据列表
    private boolean isHasChildren(Long pid) {
        QueryWrapper<Dict> queryWrapper=new QueryWrapper<Dict>();
        queryWrapper.eq("parent_id",pid);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count >0;
    }
}
