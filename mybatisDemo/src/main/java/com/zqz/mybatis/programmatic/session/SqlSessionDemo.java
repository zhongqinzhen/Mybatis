package com.zqz.mybatis.programmatic.session;

import com.zqz.mybatis.programmatic.dao.TagMapper;
import com.zqz.mybatis.programmatic.model.Tag;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by zhongqinzhen on 2018/7/9.
 */
public class SqlSessionDemo {

    public static void main(String[] args) {

        SqlSession sqlSession = getSession();
        TagMapper tagMapper = sqlSession.getMapper(TagMapper.class);

//        Tag tag = new Tag();
//        tag.setCreateTime(new Date());
//        tag.setUpdateTime(new Date());
//        tag.setTagName("test....");
//        tag.setTagType("unknow...");
//        tagMapper.insert(tag);

        Tag tag = tagMapper.selectByPrimaryKey(2L);
        System.out.println(tag);
    }

    public static SqlSession getSession(){

        String xmlPath = "/Users/zhongqinzhen/workspace/study/mybatisDemo/src/main/java/com/zqz/mybatis/" +
                "programmatic/mybatis-conf.xml";

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);

        return sqlSessionFactory.openSession();

    }


}
