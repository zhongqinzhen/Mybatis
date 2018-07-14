package com.zqz.mybatis.customer;

import java.lang.annotation.*;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Namespace {
    String value();
}
