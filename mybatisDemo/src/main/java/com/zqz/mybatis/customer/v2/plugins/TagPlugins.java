package com.zqz.mybatis.customer.v2.plugins;

import com.zqz.mybatis.customer.v2.executor.Executor;

/**
 * Created by zhongqinzhen on 2018/7/14.
 */


@CustomerPlugins(
        method = "insert",
        clazzType = Executor.class
)
public class TagPlugins implements CustomerInterceptor {

    @Override
    public void executor() {
        System.out.println("com.zqz.mybatis.customer.v2.executor.Executor.selectOne executor");
    }
}
