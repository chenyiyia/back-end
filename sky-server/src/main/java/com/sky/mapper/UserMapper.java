package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据id查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select count(id) from user where date(create_time) = #{localDate}")
    Long getSumNewUserByOrderTime(LocalDate localDate);

    @Select("select count(id) from user where create_time >= #{begin} and create_time <= #{end}")
    Integer countByMap(Map map);
}
