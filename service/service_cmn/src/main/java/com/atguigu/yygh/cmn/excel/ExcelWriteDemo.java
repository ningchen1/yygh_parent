package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import java.util.ArrayList;
import java.util.List;

/*====================================================
                时间: 2022-05-24
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
public class ExcelWriteDemo {

    //方式二：往多个sheet中写数据
    public static void main(String[] args) {
        List<Student> students=new ArrayList<Student>();
        students.add(new Student(1,"朱晓溪",18,true));
        students.add(new Student(2,"常永亮",18,true));
        students.add(new Student(3,"段磊",18,true));
        students.add(new Student(4,"田佳",18,true));

        List<Student> studentList=new ArrayList<Student>();
        studentList.add(new Student(5,"梁启晨",18,true));
        studentList.add(new Student(6,"王志峰",18,true));


        ExcelWriter excelWriter = EasyExcel.write("C:\\Users\\LH\\Desktop\\abc.xlsx", Student.class).build();
        WriteSheet sheet1 = EasyExcel.writerSheet(0, "学生列表1").build();
        WriteSheet sheet2 = EasyExcel.writerSheet(1, "学生列表2").build();
        excelWriter.write(students,sheet1);
        excelWriter.write(studentList,sheet2);

        //关闭
        excelWriter.finish();

    }

    //方式一：往单个sheet中写数据
//    public static void main(String[] args) {
//        List<Student> students=new ArrayList<Student>();
//        students.add(new Student(1,"朱晓溪",18,true));
//        students.add(new Student(2,"常永亮",18,true));
//        students.add(new Student(3,"段磊",18,true));
//        students.add(new Student(4,"田佳",18,true));
//        //这种方式：只能是往单个sheet中写数据
//        EasyExcel.write("C:\\Users\\LH\\Desktop\\hello.xlsx",Student.class).sheet("学生列表1").doWrite(students);
//    }


}
