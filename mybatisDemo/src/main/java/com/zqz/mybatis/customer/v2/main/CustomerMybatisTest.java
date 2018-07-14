package com.zqz.mybatis.customer.v2.main;

import com.zqz.mybatis.customer.Insert;
import com.zqz.mybatis.customer.Namespace;
import com.zqz.mybatis.customer.Select;
import com.zqz.mybatis.customer.common.mapper.TagMapper;
import com.zqz.mybatis.customer.common.model.Tag;
import com.zqz.mybatis.customer.v2.configuration.CRUDEnum;
import com.zqz.mybatis.customer.v2.configuration.CustomerConfigurationV2;
import com.zqz.mybatis.customer.v2.configuration.MapperData;
import com.zqz.mybatis.customer.v2.executor.CacheExecutor;
import com.zqz.mybatis.customer.v2.executor.SimpleExecutor;
import com.zqz.mybatis.customer.v2.plugins.CustomerInterceptor;
import com.zqz.mybatis.customer.v2.plugins.CustomerPlugins;
import com.zqz.mybatis.customer.v2.session.CustomerSqlSessionV2;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CustomerMybatisTest {

    public static void main(String[] args) {

        CustomerConfigurationV2 customerConfigurationV2 = scanMapperLocation("com.zqz.mybatis.customer.common.mapper");
        scanCustomerInterceptor("com.zqz.mybatis.customer.v2.plugins",customerConfigurationV2);
        CustomerSqlSessionV2 customerSqlSessionV2 = new CustomerSqlSessionV2(customerConfigurationV2,new CacheExecutor(customerConfigurationV2));

        TagMapper tagMapper = customerSqlSessionV2.getMapper(TagMapper.class);
        Tag tag = tagMapper.selectByPrimaryKey(3L);

        System.out.println("result = " + tag);

//        tag = tagMapper.selectByPrimaryKey(4L);
//
//        System.out.println(tag);
//
//        tag = tagMapper.selectByPrimaryKey(3L);
//
//        System.out.println(tag);

//        System.out.println(tag);
//
//        tag.setTagName("111");
//        tag.setTagType("222");
//        int result = tagMapper.insert(tag);
//        System.out.println("operate over ,result = " + result);


    }

    private static CustomerConfigurationV2 scanMapperLocation(String scanPath){
        Reflections reflections = new Reflections(scanPath);

        CustomerConfigurationV2 customerConfigurationV2 = new CustomerConfigurationV2();

        /** 查找所有以Namespace为类注解的类，如果没有用Namespace注解，代表不是要扫描的类*/
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Namespace.class);

        for (Class clazz : classes){

            Namespace namespace = (Namespace) clazz.getAnnotation(Namespace.class);

            try {
                /** 添加绑定关系，但是如果配置的值无法反射成Class类，忽略*/
                Class mapperClazz = Class.forName(namespace.value());
                customerConfigurationV2.addMapper(mapperClazz);

            } catch (ClassNotFoundException e) {
                continue;
            }

            Method[] methods = clazz.getMethods();

            for (Method method : methods){
                Annotation annotation = method.getAnnotations()[0];

                String fullyMethodName = clazz.getName() + "." + method.getName();
                if (annotation instanceof Insert){
                    Insert insert = (Insert) annotation;
                    customerConfigurationV2.addMapperData(fullyMethodName,buildMapperData(insert.value(),null,CRUDEnum.INSERT));
                    continue;
                } else if(annotation instanceof Select){
                    Select select = (Select) annotation;
                    customerConfigurationV2.addMapperData(fullyMethodName,buildMapperData(select.value(),
                            method.getReturnType(),CRUDEnum.SELECTONE));
                    continue;
                }
            }
        }

        return customerConfigurationV2;
    }

    private static MapperData buildMapperData(String sql, Class resultType, CRUDEnum crudEnum){
        MapperData mapperData = new MapperData();
        mapperData.setStatementSql(sql);
        mapperData.setResultType(resultType);
        mapperData.setCrudEnum(crudEnum);
        return mapperData;
    }

    private static void scanCustomerInterceptor(String scanPath,CustomerConfigurationV2 customerConfigurationV2){
        Reflections reflections = new Reflections(scanPath);

        /** 查找所有以CustomerPlugins为类注解的类，如果没有用CustomerPlugins注解，代表不是要扫描的类*/
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CustomerPlugins.class);

        for (Class clazz : classes){
            try {
                customerConfigurationV2.addInterceptor((CustomerInterceptor) clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
