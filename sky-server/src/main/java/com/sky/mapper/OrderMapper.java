package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    /**
     * 根据订单状态和超市时间处理订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 更新订单
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Page<Orders> pageQueryByUserIdAndStatus(Long userId, Integer status);

    Page<Orders> conditionQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    Integer countStatus(Integer status);

    @Select("select sum(amount) from orders where date(order_time) = #{localDate}")
    BigDecimal getSumAmoutByOrderTime(LocalDate localDate);

    @Select("select sum(user_id) from orders where date(order_time) = #{localDate}")
    Long getSumUserByOrderTime(LocalDate localDate);

    Integer countByMap(Map map);

    Double sumByMap(Map map);
}
