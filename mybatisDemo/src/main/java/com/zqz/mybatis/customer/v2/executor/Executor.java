package com.zqz.mybatis.customer.v2.executor;

import com.zqz.mybatis.customer.v2.configuration.MapperData;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public interface Executor {
    public <T> T selectOne(MapperData mapperData, String parameter);

    public int insert(MapperData mapperData, Object parameter);

}
