package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品: {}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //清理redis
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品批量删除，删一个也能用")
    Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除：{}",ids);
        dishService.deleteBatch(ids);

        //清理redis
        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    Result<DishVO> getById(@PathVariable Long id){
        log.info("菜品信息：{}",id);
        Result<DishVO> result=dishService.getByIdWithFlavor(id);
        return result;
    }

    @PutMapping
    @ApiOperation("修改菜品")
    Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品： {}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //清理redis
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     *根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    Result<List<DishVO>> list(Long categoryId){
        //1.获取传输对象 动态查询,只能查在售的
        Dish dish=new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        //2.执行查询
        List<DishVO> list = dishService.listWithFlavor(dish);
        //3.回显对象
        return Result.success(list);
    }

    /**
     * 修改起售停售
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改起售停售")
    Result startOrStop(@PathVariable Integer status,Long id){
        //1.获取传输对象
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        //2.执行修改
        dishService.startOrStop(dish);

        //清理redis
        cleanCache("dish_*");

        //3.回显对象
        return Result.success();
    }

    /**
     * 清理缓存
     */
    private void cleanCache(String pattern){
        Set<String> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
