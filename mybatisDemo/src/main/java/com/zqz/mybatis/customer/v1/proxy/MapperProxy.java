package com.zqz.mybatis.customer.v1.proxy;

import com.zqz.mybatis.customer.Insert;
import com.zqz.mybatis.customer.Namespace;
import com.zqz.mybatis.customer.Select;
import com.zqz.mybatis.customer.v1.session.CustomerSqlSession;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class MapperProxy implements InvocationHandler{

    private CustomerSqlSession sqlSession;

    public MapperProxy(CustomerSqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /** 获取方法对应的类*/
        Class clazz = method.getDeclaringClass();

        /** 获取类上面指定的namespace注解*/
        Namespace namespace = (Namespace) clazz.getAnnotation(Namespace.class);

        /** 如果类上的namespace跟类的路径不一致，直接调用相应的方法
         *  这里只是为了仿照Mybatis上namespace的校验，实际并不是这么用的
         *
         *  实际上Mybatis是在映射MapperProxy的时候就已经继续校验了，
         *      而校验逻辑则是调用getMapper方法时检查该Class是否有指定的MapperProxyFactory
         *      而添加逻辑这是在进行mapper.xml解析的时候，有一步绑定namespace的时候构建相应的MapperProxyFactory
         *      实际上就是将Class跟MapperProxyFactory通过Map对象映射起来
         *
         *  如果namespace校验通过，则判断方法上的注解，执行不同的CRUD操作
         * */
        if (namespace != null && namespace.value().equalsIgnoreCase(clazz.getName())){

            Annotation[] annotations = method.getAnnotations();

            String sqlStatement = null;

            Annotation annotation = annotations[0];

            if (annotation instanceof Select){
                sqlStatement = ((Select)annotation).value();
                return sqlSession.selectOne(sqlStatement, String.valueOf(args[0]));
            } else if (annotation instanceof Insert){
                sqlStatement = ((Insert)annotation).value();
                return sqlSession.insert(sqlStatement,args[0]);
            }

            return null;

        }

        return method.invoke(proxy,args);
    }
}
