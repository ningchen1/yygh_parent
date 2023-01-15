package com.atguigu.yygh.hosp.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/*====================================================
                时间: 2022-05-25
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@Data
@NoArgsConstructor
@AllArgsConstructor //pojo类对应的集合(首字母小写):
//@Document(value = "Actor")
public class Actor {

//    @Id //当前属性和mongodb集合中的主键是对应的
//    private String aaaid; //id 对应mongodb中集合的_id字段有一个对应关系:mongotemplate

    private String id;
    private String actorName;
    private Boolean gender;
    private Date birth;
    private Integer age;
}
