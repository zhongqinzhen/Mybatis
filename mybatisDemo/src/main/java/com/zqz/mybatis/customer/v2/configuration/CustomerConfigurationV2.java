package com.zqz.mybatis.customer.v2.configuration;

import com.zqz.mybatis.customer.v2.plugins.CustomerInterceptor;
import com.zqz.mybatis.customer.v2.proxy.MapperProxyV2;
import com.zqz.mybatis.customer.v2.session.CustomerSqlSessionV2;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CustomerConfigurationV2 {

    private Map<Class, MapperProxyV2> knowMapperProxyMap = new HashMap<>();
    private Map<String, MapperData> fullyMethodMapperDataMap = new HashMap<>();

    private List<CustomerInterceptor> interceptorList = new ArrayList<>();

    public <T> T getMapper(Class<T> clazz, CustomerSqlSessionV2 sqlSession) {

        MapperProxyV2 mapperProxy = knowMapperProxyMap.get(clazz);

        if (mapperProxy == null){
            throw new RuntimeException("未找到对应class【"+clazz.getName()+"】对应的MapperProxy对象");
        }

        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{clazz},mapperProxy.setSqlSession(sqlSession));
    }

    public void addMapper(Class namespace){

        if (knowMapperProxyMap.containsKey(namespace)){
            throw new RuntimeException("namespace【" + namespace+"】已经存在，请检查。");
        }

        knowMapperProxyMap.put(namespace,new MapperProxyV2());
    }

    public void addMapperData(String fullyMethodName, MapperData mapperData){
        if (fullyMethodMapperDataMap.containsKey(fullyMethodName)){
            return;
        }

        fullyMethodMapperDataMap.put(fullyMethodName,mapperData);
    }

    public Map<String, MapperData> getFullyMethodMapperDataMap() {
        return fullyMethodMapperDataMap;
    }


    public void addInterceptor(CustomerInterceptor interceptor){
        if (!interceptorList.contains(interceptor)){
            interceptorList.add(interceptor);
        }
    }

    public List<CustomerInterceptor> getInterceptorList() {
        return interceptorList;
    }

    @Override
    public String toString() {
        return "CustomerConfigurationV2{" +
                "knowMapperProxyMap=" + knowMapperProxyMap +
                ", fullyMethodMapperDataMap=" + fullyMethodMapperDataMap +
                '}';
    }
}
