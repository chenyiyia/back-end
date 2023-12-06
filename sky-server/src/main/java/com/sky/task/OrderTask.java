package com.sky.task;

import com.sky.constant.OrderConstant;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 *定时任务类：定时处理订单状态
 */
//@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;

    /**
     * 处理超时订单
     */
    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0/5 * * * * ?")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        //1.查询超市订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT,time);
        //2.如果不为空，则一个一个删除，并且删除详细订单
        if(ordersList != null && ordersList.size()>0){
            ordersList.forEach(orders -> {
                //动态修改
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(OrderConstant.ORDER_TIME_OUT);
                orders.setCancelTime(time);
                orderMapper.update(orders);
            });
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    //@Scheduled(cron = "0 0 1 * * ? ")
    @Scheduled(cron = "1/5 * * * * ? ")
    public void processDeliveryOrder(){
        log.info("定时处理派送中的订单：{}",LocalDateTime.now());
        //1.查询处于派送中的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,time);
        //2.如果不为空则动态更新订单
        if(ordersList != null && ordersList.size()>0){
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            });
        }
    }

}
