package com.zqz.mybatis.customer.v2.resultset;

import com.zqz.mybatis.customer.common.model.Tag;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhongqinzhen on 2018/7/13.
 */
public class ResultSetHandler {

    private static Map<String,String> handlerTypeMap = new HashMap<>();

    static {
        handlerTypeMap.put("id","id");
        handlerTypeMap.put("tagName","tag_name");
        handlerTypeMap.put("tagType","tag_type");
        handlerTypeMap.put("createTime","create_time");
        handlerTypeMap.put("updateTime","update_time");
    }

    public <T> T handler(ResultSet resultSet, Class resultType) {

        try {
            Object obj = resultType.newInstance();

            if (resultSet.next()){
                for (Field field : resultType.getDeclaredFields()){
                    setValue(obj,field,resultSet,resultType);
                }
            }

            return (T) obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private void setValue(Object obj,Field field,ResultSet resultSet,Class resultType){

        String fieldName = field.getName();

        String methodName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1,field.getName().length());

        try {

            Object dbVal = resultSet.getObject(handlerTypeMap.get(fieldName));

            resultType.getMethod(methodName,field.getType()).invoke(obj,dbVal);
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

}
