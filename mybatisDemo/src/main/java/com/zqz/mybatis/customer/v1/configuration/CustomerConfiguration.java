package com.zqz.mybatis.customer.v1.configuration;


import com.zqz.mybatis.customer.v1.proxy.MapperProxy;
import com.zqz.mybatis.customer.v1.session.CustomerSqlSession;

import java.lang.reflect.Proxy;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CustomerConfiguration {

    public <T> T getMapper(Class<T> clazz, CustomerSqlSession sqlSession) {

        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{clazz},new MapperProxy(sqlSession));
    }
}
