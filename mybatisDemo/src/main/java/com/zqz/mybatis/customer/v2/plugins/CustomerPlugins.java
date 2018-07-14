package com.zqz.mybatis.customer.v2.plugins;

import java.lang.annotation.*;

/**
 * Created by zhongqinzhen on 2018/7/14.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CustomerPlugins {
    String method();
    Class clazzType();
}
