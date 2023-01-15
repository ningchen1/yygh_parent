package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.ServiceHospMainStarter;
import com.atguigu.yygh.hosp.bean.Actor;
import com.mongodb.client.result.DeleteResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;
import java.util.regex.Pattern;

/*====================================================
                时间: 2022-05-25
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@SpringBootTest//(classes = ServiceHospMainStarter.class)
public class AppTest {


    /*=============================================
         使用MongoTemplate三步：
           1.引入starter依赖
           2.配置mongodb连接信息
           3. 在使用的地方直接注入MongoTemplate对象
      ============================================*/
    @Autowired
    private MongoTemplate mongoTemplate;

    //分页查询
    @Test
    public void testQueryPage(){
        int pageNum=1;
        int size=3;

        Query query=new Query(Criteria.where("gender").is(false));
        long total = mongoTemplate.count(query, Actor.class);

        List<Actor> actors = mongoTemplate.find(query.skip((pageNum-1)*size).limit(size), Actor.class);



        Map<String,Object> map = new HashMap<String,Object>();
        map.put("total",total);
        map.put("rows",actors);

        System.out.println(total);



    }
    //查询
    @Test
    public void testQuery(){
//        Actor actor = mongoTemplate.findById("628dd85dfd93786a5ea4359a", Actor.class);
//        System.out.println(actor);

//        List<Actor> all = mongoTemplate.findAll(Actor.class);
//        for (Actor actor : all) {
//            System.out.println(actor);
//        }

        //Query query=new Query(Criteria.where("age").is(19));

        String result = String.format("%s%s%s", ".*", "j", ".*");
        Pattern pattern=Pattern.compile(result,Pattern.CASE_INSENSITIVE);
        Query query=new Query(Criteria.where("actorName").regex(pattern));
        List<Actor> actors = mongoTemplate.find(query, Actor.class);
        for (Actor actor : actors) {
            System.out.println(actor);
        }
    }


      //修改:upsert:
     //   updateFirst:只修改符合条件的第一个文档
      //  updateMulti:修改所有符合条件的文档数据
    @Test
    public void testUpdate(){
        Query query=new Query(Criteria.where("gender").is(true));

        Update update=new Update();
        update.set("age",19);

        mongoTemplate.updateMulti(query,update,Actor.class);
    }

    //删除: and 关系： Criteria.where("gender").is(false).and("actorName").is("张敏")
           //or关系:   Criteria criteria=new Criteria(); criteria.orOperator..
    @Test
    public void testDelete(){
        Criteria criteria=new Criteria();
        criteria.orOperator(Criteria.where("_id").is("1"),Criteria.where("actorName").is("关之琳"));

        Query query=new Query(criteria);

        DeleteResult remove = mongoTemplate.remove(query, Actor.class);
        System.out.println(remove.getDeletedCount());
    }

    //新增：可以使用insert、save方法
    // insert只能做添加，不能做修改,可以批量添加数据
    // save既可以做添加，也可以做修改,如果我们要使用save修改集合中某个字段的值，其它字段保持原来的值，必须是先查询出来，然后修改，最后写回去。

    @Test
    public void testBatchInsert(){
        List<Actor> actors=new ArrayList<Actor>();
//        actors.add(new Actor("11","Jerry",false,new Date()));
//        actors.add(new Actor("15","Tom",false,new Date()));
//        actors.add(new Actor("13","Andy",false,new Date()));
//        actors.add(new Actor("14","jack",false,new Date()));

      mongoTemplate.insert(actors,Actor.class);
    }

    @Test
    public void testModify(){
        Actor actor = mongoTemplate.findById("1", Actor.class);
        actor.setActorName("朱丽倩");
        mongoTemplate.save(actor);
    }
    @Test
    public void testInsert(){
       // mongoTemplate.insert(new Actor("1","刘德华",true,new Date()));
        Actor actor = new Actor();
        actor.setId("2");
        actor.setActorName("郭富城");



        mongoTemplate.save(actor);
    }
}
