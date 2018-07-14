package com.zqz.mybatis.customer.v1.main;

import com.zqz.mybatis.customer.common.model.Tag;
import com.zqz.mybatis.customer.v1.configuration.CustomerConfiguration;
import com.zqz.mybatis.customer.v1.executor.CustomerExecutor;
import com.zqz.mybatis.customer.v1.session.CustomerSqlSession;
import com.zqz.mybatis.customer.common.mapper.TagMapper;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CustomerMybatisTest {

    public static void main(String[] args) {
        CustomerSqlSession sqlSession = new CustomerSqlSession(new CustomerConfiguration(),
                new CustomerExecutor());

        TagMapper tagMapper = sqlSession.getMapper(TagMapper.class);
        Tag tag = tagMapper.selectByPrimaryKey(2L);
        System.out.println(tag);

        tag.setTagName("tagName1");
        tag.setTagType("tagType2");
        int result = tagMapper.insert(tag);

        System.out.println("operate over ,result = " + result);
    }
}
