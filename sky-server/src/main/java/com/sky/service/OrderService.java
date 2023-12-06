package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    void remeider(Long id);

    PageResult historyOrders(Integer page, Integer pageSize,Integer status);

    OrderVO detail(Long id);

    void cancelById(Long id);

    void repetition(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

    OrderVO details(Long id);

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void cancel(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);
}
