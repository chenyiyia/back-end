package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Api("订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("条件查询")
    Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        //1.获取传输对象
        //2.执行查询
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        //3.回显对象
        return Result.success(pageResult);
    }

    /**
     * 统计数量
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("统计数量")
    Result<OrderStatisticsVO> statistics(){
        //1.获取传输对象
        //2.执行统计
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        //3.回显对象
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查看详细
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查看详细")
    Result<OrderVO> details(@PathVariable Long id){
        //1.获取传输对象
        //2.执行查找
        OrderVO orderVO = orderService.details(id);
        //3.回显对象
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        //1.获取传输对象
        //2.执行修改
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        orderService.confirm(ordersConfirmDTO);
        //3.回显对象
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        //1.获取传输对象
        //2.执行修改
        orderService.rejection(ordersRejectionDTO);
        //3.回显对象
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    Result cacel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        //1.获取传输对象
        //2.执行修改
        orderService.cancel(ordersCancelDTO);
        //3.回显对象
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    Result delivery(@PathVariable Long id){
        //1.获取传输对象
        //2.执行修改
        orderService.delivery(id);
        //3.回显对象
        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    Result complete(@PathVariable Long id){
        //1.获取传输对象
        //2.执行修改
        orderService.complete(id);
        //3.回显对象
        return Result.success();
    }
}
