package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*====================================================
                时间: 2022-05-24
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/

public class StudentListener  extends AnalysisEventListener<Student> {


    private static final int BATCH_COUNT = 10;
    List<Student> list = new ArrayList<Student>();

    //每解析excel文件中的一行数据，都会调用一次invoke方法
    @Override
    public void invoke(Student student, AnalysisContext analysisContext) {
       //批量操作
        //list.add(student);
//        if(list.size() >= BATCH_COUNT){
//            baseMapper.batchInsert(list);
//            list.clear();
//        }
        System.out.println(student);
    }

    //当解析excel文件某个sheet的标题的时候
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("标题为："+headMap);
    }

    //当excel文件被解析完毕之后，走这个方法：收尾工作，关闭连接
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
       //baseMapper.batchInsert(list);
    }

}
