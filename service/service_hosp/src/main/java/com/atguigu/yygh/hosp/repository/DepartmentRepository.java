package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

/*************************************************
 时间: 2022-05-27
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
public interface DepartmentRepository  extends MongoRepository<Department,String> {
    Department findByHoscodeAndDepcode(String hoscode, String depcode);

}
