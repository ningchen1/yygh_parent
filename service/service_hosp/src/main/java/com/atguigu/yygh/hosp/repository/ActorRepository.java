package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.bean.Actor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/*************************************************
 时间: 2022-05-25
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
public interface ActorRepository  extends MongoRepository<Actor,String> {

    public List<Actor> findByActorNameLikeAndGender(String name,Boolean gender);
}
