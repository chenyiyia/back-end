package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    public List<ShoppingCart> list(ShoppingCart shoppingCart);

    void updateById(ShoppingCart shoppingCart1);

    void insert(ShoppingCart shoppingCart1);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
