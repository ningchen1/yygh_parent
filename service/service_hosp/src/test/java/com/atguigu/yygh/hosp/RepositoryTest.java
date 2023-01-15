package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.bean.Actor;
import com.atguigu.yygh.hosp.repository.ActorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/*====================================================
                时间: 2022-05-25
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@SpringBootTest
public class RepositoryTest {

    @Autowired
    private ActorRepository actorRepository;


    @Test
    public void testSelfDefinitionMethod(){
        List<Actor> actors = actorRepository.findByActorNameLikeAndGender("龙",true);
        for (Actor actor : actors) {
            System.out.println(actor);
        }
    }

    @Test
    public void testPage(){
        int pageNum=0;//0表示第一页
        int pagesize=3;
        Actor actor=new Actor();
        actor.setAge(44);

        Example<Actor> example=Example.of(actor);
        Pageable pageable= PageRequest.of(pageNum,pagesize);
        Page<Actor> page = actorRepository.findAll(example, pageable);
        System.out.println("总记录数："+page.getTotalElements());
        System.out.println("总页数："+page.getTotalPages());
        System.out.println("当前页列表数据：");
        for (Actor actor1 : page.getContent()) {
            System.out.println(actor1);
        }
    }
    @Test
    public void testQuery(){
//        Actor actor = actorRepository.findById("30").get();
//        System.out.println(actor);

        Actor actor=new Actor();
        //actor.setGender(true);
        actor.setActorName("富");

        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
               // .withMatcher("actorName", ExampleMatcher.GenericPropertyMatchers.startsWith())
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        Example<Actor> example=Example.of(actor,matcher);

        List<Actor> all = actorRepository.findAll(example);
        for (Actor actor1 : all) {
            System.out.println(actor1);
        }
    }
    //修改
    @Test
    public void testUpdate(){
        Actor actor=new Actor();
        actor.setId("31");
        actor.setActorName("1刘德华");
        actorRepository.save(actor);
    }

    //删除
    @Test
    public void testDelete(){
       // actorRepository.deleteById("628dd85dfd93786a5ea4359a");
        Actor actor = new Actor();
        actor.setId("32");
        actorRepository.delete(actor);
    }

    @Test
    public void testInsert(){
        //actorRepository.insert(new Actor("16","振忠1",true,new Date()));
        //actorRepository.save(new Actor("17","文博1",true,new Date()));
        List<Actor> actorList=new ArrayList<Actor>();
//        actorList.add(new Actor("30","周润发",true,new Date()));
//        actorList.add(new Actor("31","周星驰",true,new Date()));
//        actorList.add(new Actor("32","李连杰",true,new Date()));
        actorList.add(new Actor("40","嘿嘿", true,new Date(),44));

       // actorRepository.insert(actorList);
        actorRepository.saveAll(actorList);
    }
}
