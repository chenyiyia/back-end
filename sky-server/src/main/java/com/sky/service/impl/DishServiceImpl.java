package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //菜品表插入1条
        dishMapper.insert(dish);

        Long dishId=dish.getId();

        //口味表插入n条
        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        if(dishFlavors!=null && dishFlavors.size()!=0){
            dishFlavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品批量删除，删一个也能用
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //1.判断是否是起售中的商品
        List<Integer> listStatus = dishMapper.getBatchStatusByIds(ids);
        for(Integer statues : listStatus){
            if(statues != null && statues == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        //2.判断是否被套餐关联了
        List<Long> listSetmealId=setmealDishMapper.getBatchSetmealIdsByDishIds(ids);
        if(listSetmealId != null && listSetmealId.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除菜品表中的菜品数据
        dishMapper.deleteBatch(ids);
        //4.删除菜品关联的口味
        dishFlavorMapper.deleteBatch(ids);
    }

    /**
     * 根据id获得VO对象
     * @param id
     * @return
     */
    public Result<DishVO> getByIdWithFlavor(Long id) {
        //1.执行查询 两个查询
        Dish dish=dishMapper.getById(id);//1.1查询菜品
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);//1.2查询口味
        //2.回显对象
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return Result.success(dishVO);
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //1.获取传输对象
        //2.执行修改 两个修改a
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);//2.1修改菜品

        dishFlavorMapper.deleteById(dishDTO.getId());//2.2修改多个口味 修改之前要先批量删除旧的
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(dishFlavors != null){
            dishFlavors.forEach(disFlavor -> {
                disFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(dishFlavors);
        }
        //3.修改不回显
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
