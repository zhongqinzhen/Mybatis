package com.zqz.mybatis.customer.v2.proxy;

import com.zqz.mybatis.customer.v2.configuration.CRUDEnum;
import com.zqz.mybatis.customer.v2.configuration.MapperData;
import com.zqz.mybatis.customer.v2.session.CustomerSqlSessionV2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class MapperProxyV2 implements InvocationHandler{

    private CustomerSqlSessionV2 sqlSession;

    public MapperProxyV2() {
    }

    public MapperProxyV2(CustomerSqlSessionV2 sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /** 获取方法对应的类*/
        Class clazz = method.getDeclaringClass();


        String fullyMethodName = clazz.getName() + "." + method.getName();
        MapperData mapperData =
                sqlSession.getConfiguration().getFullyMethodMapperDataMap().get(fullyMethodName);

        if (mapperData == null){
            return null;
        }

        if (mapperData.getCrudEnum().equals(CRUDEnum.INSERT)){
            return sqlSession.insert(mapperData,args[0]);
        } else if (mapperData.getCrudEnum().equals(CRUDEnum.SELECTONE)){
            return sqlSession.selectOne(mapperData,String.valueOf(args[0]));
        }

        return method.invoke(proxy,args);
    }


    public MapperProxyV2 setSqlSession(CustomerSqlSessionV2 sqlSession) {
        this.sqlSession = sqlSession;
        return this;
    }
}
