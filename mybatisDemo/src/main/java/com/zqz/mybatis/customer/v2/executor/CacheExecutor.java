package com.zqz.mybatis.customer.v2.executor;


import com.zqz.mybatis.customer.v2.configuration.CustomerConfigurationV2;
import com.zqz.mybatis.customer.v2.configuration.MapperData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CacheExecutor implements Executor{

    private Executor delagate;

    private Map<String,Object> cacheMap = new HashMap<>();


    public CacheExecutor(CustomerConfigurationV2 configuration) {
        delagate = new SimpleExecutor(configuration);
    }

    @Override
    public <T> T selectOne(MapperData mapperData, String parameter) {

        if (cacheMap.containsKey(mapperData.getStatementSql().concat(parameter))){
            return (T) cacheMap.get(mapperData.getStatementSql().concat(parameter));
        }

        Object object = delagate.selectOne(mapperData, parameter);

        cacheMap.put(mapperData.getStatementSql().concat(parameter),object);

        return (T) object;
    }

    @Override
    public int insert(MapperData mapperData, Object parameter) {

        return delagate.insert(mapperData,parameter);
    }

}
