package com.atguigu.yygh.hosp.utlis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
