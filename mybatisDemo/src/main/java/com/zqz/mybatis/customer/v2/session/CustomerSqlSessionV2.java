package com.zqz.mybatis.customer.v2.session;


import com.zqz.mybatis.customer.v2.configuration.CustomerConfigurationV2;
import com.zqz.mybatis.customer.v2.configuration.MapperData;
import com.zqz.mybatis.customer.v2.executor.Executor;
import com.zqz.mybatis.customer.v2.executor.SimpleExecutor;

/**
 * Created by zhongqinzhen on 2018/7/9.
 */
public class CustomerSqlSessionV2 {
    private CustomerConfigurationV2 configuration;

    private Executor executor;

    public <T> T getMapper(Class<T> clazz){
        return configuration.getMapper(clazz,this);
    }

    public <T> T selectOne(MapperData mapperData, String parameter){
        return executor.selectOne(mapperData,parameter);
    }

    public int insert(MapperData mapperData, Object parameter){
        return executor.insert(mapperData,parameter);
    }

    public CustomerSqlSessionV2(CustomerConfigurationV2 configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    public CustomerConfigurationV2 getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CustomerConfigurationV2 configuration) {
        this.configuration = configuration;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
