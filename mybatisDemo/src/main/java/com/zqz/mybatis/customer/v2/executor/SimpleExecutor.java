package com.zqz.mybatis.customer.v2.executor;


import com.zqz.mybatis.customer.common.model.Tag;
import com.zqz.mybatis.customer.v2.configuration.CustomerConfigurationV2;
import com.zqz.mybatis.customer.v2.configuration.MapperData;
import com.zqz.mybatis.customer.v2.plugins.CustomerInterceptor;
import com.zqz.mybatis.customer.v2.plugins.CustomerPlugins;
import com.zqz.mybatis.customer.v2.statement.StatementHandler;

import java.util.List;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class SimpleExecutor implements Executor{

    private CustomerConfigurationV2 configuration;

    public SimpleExecutor(CustomerConfigurationV2 configuration) {
        this.configuration = configuration;
    }

    private StatementHandler handler = new StatementHandler();

    private void doPlugin(String methodName){


        for (CustomerInterceptor interceptor : configuration.getInterceptorList()){
            CustomerPlugins annotation = interceptor.getClass().getAnnotation(CustomerPlugins.class);

            if (annotation == null){
                continue;
            }

            if (methodName.equals(annotation.method()) &&
                    annotation.clazzType().isAssignableFrom(this.getClass())){
                interceptor.executor();
            }
        }

    }


    @Override
    public <T> T selectOne(MapperData mapperData, String parameter) {

        System.out.println("select in db,SQL : " + mapperData.getStatementSql());


        String method = Thread.currentThread() .getStackTrace()[1].getMethodName();

        this.doPlugin(method);

        List<T> tList = handler.<T>query(mapperData.getStatementSql(),parameter,mapperData.getResultType());

        if (tList == null || tList.size() == 0){
            return null;
        }


        return tList.get(0);
    }

    @Override
    public int insert(MapperData mapperData, Object parameter) {

        if (mapperData == null || parameter == null || !(parameter instanceof Tag)){
            return 0;
        }

        this.doPlugin(this.getClass().getName());

        return handler.execute(mapperData.getStatementSql(),parameter);
    }


}
