package com.sky.controller.user;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api("订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        //1.获取传输对象
        log.info("用户下单：{}",ordersSubmitDTO);
        //2.执行提交
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        //3.回显数据
        return Result.success(orderSubmitVO);
    }

    /**
     * 用户催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单接口")
    Result reminder(@PathVariable Long id){
        //1.获取传输对象
        //2.执行催单
        orderService.remeider(id);
        //3.回显对象
        return Result.success();
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    Result<PageResult> historyOrders(Integer page, Integer pageSize, Integer status){
        //1.获取传输对象
        //2.执行查询
        PageResult pageResult = orderService.historyOrders(page,pageSize,status);
        //3.回显对象
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    Result<OrderVO> orderDetail(@PathVariable("id") Long id){
        //1.获取传输对象
        //2.执行查询
        OrderVO orderVO = orderService.detail(id);
        //3.回显对象
        return Result.success(orderVO);
    }

    /**
     * 订单取消
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("订单取消")
    Result cancel(@PathVariable("id") Long id){
        //1.获取传输对象
        //2.执行修改
        orderService.cancelById(id);
        //3.回显数据
        return Result.success();
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    Result repetition(@PathVariable Long id){
        //1.获取传输对象
        //2.执行添加购物车
        orderService.repetition(id);
        //3.会先对象
        return Result.success();
    }
}
