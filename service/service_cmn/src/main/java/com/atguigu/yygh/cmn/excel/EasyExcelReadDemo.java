package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;

import java.awt.*;


public class EasyExcelReadDemo {

    //方式二：读取excel文件的多个sheet
    public static void main(String[] args) {
        ExcelReader excelReader = EasyExcel.read("C:\\Users\\柠尘\\Desktop\\abc.xlsx").build();
        ReadSheet sheet1 = EasyExcel.readSheet(0).head(Student.class).registerReadListener(new StudentListener()).build();
        ReadSheet sheet2 = EasyExcel.readSheet(1).head(Student.class).registerReadListener(new StudentListener()).build();

        excelReader.read(sheet1,sheet2);

        excelReader.finish();

    }
//    //简单读取
//    public static void main(String[] args) {
//
//        EasyExcel.read("C:\\Users\\LH\\Desktop\\hello.xlsx",Student.class,new StudentListener()).sheet(0).doRead();
//    }
}
