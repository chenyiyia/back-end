package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.service.impl.ShoppingCartServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端购物车相关接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    /**
     * 购物车添加
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("购物车添加")
    Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        //1.获取传输对象
        log.info("购物车对象：{}",shoppingCartDTO);
        //2.执行购物车添加
        shoppingCartService.add(shoppingCartDTO);
        //3.回显对象
        return Result.success();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询购物车")
    Result<List<ShoppingCart>> list(){
        //1.获取传输对象
        //2.执行查询
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        //3.回显对象
        return Result.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    Result clean(){
        //1.获取传输数据
        //2.执行清口购物车
        shoppingCartService.cleanShoppingCart();
        //3.回显对象
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除购物车商品")
    Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        //1.获取传输对象
        log.info("删除购物车中的商品：{}",shoppingCartDTO);
        //2.执行减去
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        //3.回显对象
        return Result.success();
    }
}
