package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    DishMapper dishMapper;

    /**
     * 购物车添加
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //1.获取传输对象
        //2.执行添加
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);//注意：动态查询购物车

        if(list != null && list.size()>0){//注意：判断查询的对象存在则数量加1，反之购物车加入
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartMapper.updateById(shoppingCart1);
        }else{
            if(shoppingCartDTO.getDishId() != null){//注意：判断添加的是菜品还是套餐，补足属性
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                ShoppingCart shoppingCart1 = new ShoppingCart();

                shoppingCart1.setName(dish.getName());
                shoppingCart1.setImage(dish.getImage());
                shoppingCart1.setDishFlavor(shoppingCartDTO.getDishFlavor());
                shoppingCart1.setDishId(shoppingCartDTO.getDishId());
                shoppingCart1.setAmount(dish.getPrice());
                shoppingCart1.setNumber(1);
                shoppingCart1.setUserId(BaseContext.getCurrentId());
                shoppingCart1.setCreateTime(LocalDateTime.now());

                shoppingCartMapper.insert(shoppingCart1);
            }else if(shoppingCartDTO.getSetmealId() != null){
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                ShoppingCart shoppingCart1 = new ShoppingCart();

                shoppingCart1.setImage(setmeal.getImage());
                shoppingCart1.setSetmealId(shoppingCartDTO.getSetmealId());
                shoppingCart1.setName(setmeal.getName());
                shoppingCart1.setAmount(setmeal.getPrice());
                shoppingCart1.setUserId(BaseContext.getCurrentId());
                shoppingCart1.setNumber(1);
                shoppingCart1.setCreateTime(LocalDateTime.now());

                shoppingCartMapper.insert(shoppingCart1);
            }
        }
        //3.回显对象
    }

    /**
     * 展示购物车
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        //1.获取传输对象
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //2.执行展示
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //3.回显对象
        return list;
    }

}
