package com.zqz.mybatis.customer.v1.session;


import com.zqz.mybatis.customer.v1.configuration.CustomerConfiguration;
import com.zqz.mybatis.customer.v1.executor.CustomerExecutor;

/**
 * Created by zhongqinzhen on 2018/7/9.
 */
public class CustomerSqlSession {
    private CustomerConfiguration configuration;

    private CustomerExecutor executor;

    public <T> T getMapper(Class<T> clazz){
        return configuration.getMapper(clazz,this);
    }

    public <T> T selectOne(String sqlStatement, String parameter){
        return executor.selectOne(sqlStatement,parameter);
    }

    public int insert(String sqlStatement,Object parameter){
        return executor.insert(sqlStatement,parameter);
    }

    public CustomerSqlSession(CustomerConfiguration configuration, CustomerExecutor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    public CustomerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CustomerConfiguration configuration) {
        this.configuration = configuration;
    }

    public CustomerExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(CustomerExecutor executor) {
        this.executor = executor;
    }
}
