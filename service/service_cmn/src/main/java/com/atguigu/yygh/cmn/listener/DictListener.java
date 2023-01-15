package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.Map;

/*====================================================
                时间: 2022-05-24
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/

public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    public DictListener(DictMapper dictMapper){
        this.dictMapper=dictMapper;
    }


    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict=new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);

        QueryWrapper<Dict> queryWrapper=new QueryWrapper<Dict>();
        queryWrapper.eq("id",dictEeVo.getId());
        Integer count = this.dictMapper.selectCount(queryWrapper);
        if(count > 0){
            this.dictMapper.updateById(dict);
        }else{
            this.dictMapper.insert(dict);
        }
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
