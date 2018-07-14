package com.zqz.mybatis.customer.common.mapper;

import com.zqz.mybatis.customer.Insert;
import com.zqz.mybatis.customer.Namespace;
import com.zqz.mybatis.customer.Select;
import com.zqz.mybatis.customer.common.model.Tag;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */

@Namespace("com.zqz.mybatis.customer.common.mapper.TagMapper")
public interface TagMapper {
    @Select("select * from t_tag where id = %d")
    Tag selectByPrimaryKey(Long id);

    @Insert("insert into t_tag(tag_name,tag_type,create_time,update_time) values (\"%s\",\"%s\",now(),now())")
    int insert(Tag tag);
}
