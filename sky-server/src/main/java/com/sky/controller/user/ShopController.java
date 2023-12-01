package com.sky.controller.user;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    RedisTemplate redisTemplate;


    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    Result<Integer> getStatus(){
        //1.获取传输对象
        //2.执行获取
        Integer status=(Integer) redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS);
        log.info("获取店铺营业状态：{}",status==1? "营业中" : "打烊中");
        //3.回显对象
        return Result.success(status);
    }

}
