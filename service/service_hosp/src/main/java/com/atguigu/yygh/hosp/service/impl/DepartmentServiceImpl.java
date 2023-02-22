package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl  implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void saveDepartment(Map<String, Object> stringObjectMap) {
        //将stringObjectMap转化为Department对象
        Department department = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Department.class);
        //医院编号+科室编号 联合查询
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();

        Department platformDepartment= departmentRepository.findByHoscodeAndDepcode(hoscode,depcode);
        //如果mongo中没有该科室信息，做添加操作
        if(platformDepartment == null){
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else{ //如果mongo中有该科室信息，做修改操作

            department.setCreateTime(platformDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(platformDepartment.getIsDeleted());
            department.setId(platformDepartment.getId());
            departmentRepository.save(department);
        }


    }

    @Override
    public Page<Department> getDepartmentPage(Map<String, Object> stringObjectMap) {
        //将传递过来的键取值并转化为Integer类型
        Integer page= Integer.parseInt((String)stringObjectMap.get("page"));

        Integer limit = Integer.parseInt((String)stringObjectMap.get("limit"));
        //根据医院编号的键取值
        String hoscode = (String)stringObjectMap.get("hoscode");


        //设置查询条件，根据科室所在医院编号查询科室信息
        Department department=new Department();
        department.setHoscode(hoscode);
        //查询条件
        Example<Department> example = Example.of(department);
        //查询页数和条数
        Pageable pageable= PageRequest.of(page-1,limit);
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(Map<String, Object> stringObjectMap) {
        //根据医院键取值，取医院编号和科室编号
        String hoscode=(String)stringObjectMap.get("hoscode");
        String depcode=(String)stringObjectMap.get("depcode");
        //根据医院编号和科室编号查询科室信息
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        //若不为空则根据科室id删除科室信息
        if(department != null){
            departmentRepository.deleteById(department.getId());
        }

    }

    //根据医院编号，查询医院所有科室列表
    @Override
    public List<DepartmentVo> getDepartmentList(String hoscode) {
        //构造查询条件
        Department department=new Department();
        department.setHoscode(hoscode);
        //根据医院编号查询科室信息
        Example<Department> example=Example.of(department);
        List<Department> all = departmentRepository.findAll(example);

        //map的key：就是当前科室所属大科室的编号
        //map的value:就是当前大科室底下的所有子科室信息
        //Department::getBigcode当前科室所属大科室的编号
        Map<String, List<Department>> collect = all.stream().collect(Collectors.groupingBy(Department::getBigcode));

        List<DepartmentVo> bigdepartmentVoList=new ArrayList<>();

        //collect.entrySet(),collect为map类型，是获取一个map的方法，key为大科室的编号
        for (Map.Entry<String, List<Department>> entry : collect.entrySet()) {
            DepartmentVo bigdepartmentVo=new DepartmentVo();

            //1.大科室的编号
            String bigcode = entry.getKey();
            //2.当前大科室地下所有子科室列表
            List<Department> value = entry.getValue();

            List<DepartmentVo> childDepartmentVoList=new ArrayList<>();

            //当前大科室地下所有子科室列表放入到childDepartmentVoList中
            for (Department childDepartment : value) {
                //1.创建VO对象，2.获取value（childDepartment）中的子科室编号和名称，3.将编号名称放入VO中，4.将VO放入到子集合中childDepartmentVoList
                DepartmentVo childDepartmentVo=new DepartmentVo();
                //1.当前子科室的编号
                String depcode = childDepartment.getDepcode();
                //2.当前子科室的名称
                String depname = childDepartment.getDepname();
                //将编号和名称放入到childDepartmentVo中，将childDepartmentVo放入到childDepartmentVoList中
                childDepartmentVo.setDepcode(depcode);
                childDepartmentVo.setDepname(depname);
                childDepartmentVoList.add(childDepartmentVo);


            }
            //将大科室编号和子科室名称和子节点放入到bigdepartmentVoList中
            //value.get(0)是子科室
            bigdepartmentVo.setDepcode(bigcode);
            bigdepartmentVo.setDepname(value.get(0).getBigname());
            bigdepartmentVo.setChildren(childDepartmentVoList);
            bigdepartmentVoList.add(bigdepartmentVo);

        }

        return bigdepartmentVoList;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department !=null){
            return department.getDepname();
        }
        return "";
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {

        return  departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);

    }
}
