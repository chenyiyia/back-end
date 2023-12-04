package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }


    /**
     * 员工分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //1.获取传输对象
        //2.执行查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page= setmealMapper.pageQuery(setmealPageQueryDTO);

        Long total=page.getTotal();
        List<Setmeal> records=page.getResult();
        //3.回显对象
        return new PageResult(total,records);
    }

    /**
     * 套餐新增
     * @param setmealDTO
     */
    @Transactional
    public void saveWithSetmealDish(SetmealDTO setmealDTO) {
        //1.获取传输对象
        //2.执行新增
        Setmeal setmeal=new Setmeal();//2.1新增套餐对象
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId=setmeal.getId();//注意1：要回显套餐id，设置到套餐菜品上

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();//2.2批量新增菜品对象
        if(setmealDishes != null && setmealDishes.size() > 0){//注意2：套餐菜品可能为空
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
        //3.回显对象
    }

    @Override
    public void delete(List<Long> ids) {
        //1.获取传输对象
        //2.执行删除
        ids.forEach(id -> {//2.1如果待被删除的套餐包含在售，则抛异常
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        ids.forEach(id -> {//2.2一个一个删
            setmealMapper.deleteById(id);//2.3删除套餐表和套餐菜品表
            setmealDishMapper.deleteBySetmealId(id);
        });
        //3.回显对象
    }

    @Override
    public SetmealVO getByIdWithSetmealDish(Long id) {
        //1.获取传输对象
        //2.执行查询
        Setmeal setmeal = setmealMapper.getById(id);//2.1查询套餐
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);//2.2查询套餐彩屏
        //3.回显对象
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        //1.获取传输对象
        //2.执行更新
        Setmeal setmeal = new Setmeal();//2.1修改套餐
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(setmeal.getId());//2.2根据套餐id删除套餐菜品

        Long setmealId=setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();//2.3判断套餐菜品不为空则添加
        if(setmealDishes != null && setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);//注意:套餐传输对象的套餐菜品对象没有setmealId
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
        //3.回显对象
    }

    @Override
    public void update(Setmeal setmeal) {
        //1.获取传输对象
        //2.执行修改
        if(setmeal.getStatus() == StatusConstant.ENABLE){//注意：起售套餐时，当套餐内包含未起售菜品时则抛异常
            //注意：根据套餐id，联表查询，获得菜品
            List<Dish> dishes = dishMapper.getBySetmealId(setmeal.getId());
            if(dishes != null && dishes.size()>0){
                dishes.forEach(dish -> {
                    if(dish.getStatus() == 0){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        setmealMapper.update(setmeal);
        //3.回显对象
    }
}
