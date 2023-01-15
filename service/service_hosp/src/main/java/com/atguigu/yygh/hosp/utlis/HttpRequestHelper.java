package com.atguigu.yygh.hosp.utlis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*====================================================
                时间: 2022-05-27
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
public class HttpRequestHelper {

    public static Map<String, Object> switchMap(Map<String, String[]> parameterMap) {
        Map<String, Object> resultMap=new HashMap<String, Object>();

        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();

        for (Map.Entry<String, String[]> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            resultMap.put(key,value);
        }

        return resultMap;
    }
}
