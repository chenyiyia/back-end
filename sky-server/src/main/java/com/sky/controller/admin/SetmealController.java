package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        //1.获取传输对象
        log.info("套餐分页查询：{}",setmealPageQueryDTO);
        //2.执行查询
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        //3.回显对象
        return Result.success(pageResult);
    }

    /**
     * 套餐新增
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("套餐新增接口")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        //1.获取传输对象
        log.info("套餐新增对象：{}",setmealDTO);
        //2.执行新增
        setmealService.saveWithSetmealDish(setmealDTO);
        //3.回显对象
        return Result.success();
    }

    /**
     * 套餐删除接口
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("套餐删除接口")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        //1.获取传输对象
        log.info("套餐删除对象：{}",ids);
        //2.执行删除
        setmealService.delete(ids);
        //3.回显对象
        return Result.success();
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    Result<SetmealVO> getById(@PathVariable Long id){
        //1.获取传输对象
        //2.执行查询
         SetmealVO setmealVO = setmealService.getByIdWithSetmealDish(id);
        //3.回显对象
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐接口")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        //1.获取传输对象
        log.info("修改对象：{}",setmealDTO);
        //2.执行修改
        setmealService.updateWithSetmealDish(setmealDTO);
        //3.回显对象
        return Result.success();
    }

    /**
     * 起售或停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售或停售")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id){//传输对象如何设置
        //1.获取传输对象
        //2.执行修改
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealService.update(setmeal);
        //3.回显对象
        return Result.success();
    }
}
