package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api("店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     *设置店铺的营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        //1.获取传输对象
        log.info("设置店铺营业状态：{}",status==1 ? "营业中" : "打烊中");
        //2.执行设置
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS,status);
        //3.回显对象
        return Result.success();
    }

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
