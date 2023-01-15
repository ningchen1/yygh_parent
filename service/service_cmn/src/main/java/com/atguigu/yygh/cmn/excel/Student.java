package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

/*====================================================
                时间: 2022-05-24
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
public class Student {
    @ExcelProperty(value = "学生id")
    private Integer sid;
    @ExcelProperty(value = "学生姓名")
    @ColumnWidth(value = 50)
    private String name;
    @ExcelProperty(value = "学生年龄")
    private Integer age;
    @ExcelProperty(value = "学生性别")
    private boolean gender;

    public Student() {
    }

    public Student(Integer sid, String name, Integer age, boolean gender) {
        this.sid = sid;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Student{" +
                "sid=" + sid +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                '}';
    }
}
