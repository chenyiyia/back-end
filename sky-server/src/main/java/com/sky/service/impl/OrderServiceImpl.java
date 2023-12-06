package com.sky.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1.获取传输对象
        //2.执行订单提交
        //①注意：处理业务异常（购物车为空，地址簿为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //②注意：向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        orderMapper.insert(orders);

        //③注意：向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart:shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //④注意：清空购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        //⑤注意：生成VO对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        //3.回显数据
        return orderSubmitVO;
    }

    /**
     * 客户催单
     * @param id
     */
    public void remeider(Long id) {
        //1.获取传输对象
        //2.执行查询
        //①注意：防止绕过前端直接访问后端，判断订单存不存在
        Orders orders = orderMapper.getById(id);

        //②注意：调用WebSocketServer的发送方法
        Map<String,Object> map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号："+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
        //3.回显对象
    }

    @Override
    public PageResult historyOrders(Integer page, Integer pageSize,Integer status) {
        //1.获取传输对象
        //2.执行查询
        PageHelper.startPage(page,pageSize);
        Page<Orders> pageQuery = orderMapper.pageQueryByUserIdAndStatus(BaseContext.getCurrentId(),status);

        //①注意：封装OrderVO
        List<OrderVO> list = new ArrayList<>();
        if(pageQuery != null && pageQuery.getTotal() > 0){
            pageQuery.getResult().forEach(orders -> {
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());//封装：获取详细订单

                OrderVO orderVO = new OrderVO();//封装：封装
                BeanUtils.copyProperties(orders,orderVO);//TODO
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            });
        }

        //3.回显对象
        return new PageResult(pageQuery.getTotal(),list);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    public OrderVO detail(Long id) {
        //1.获取传输对象
        //2.执行查询
        Orders orders = orderMapper.getById(id);//2.1获取订单信息

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);//2.2获取订单详细

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        //3.回显对象
        return orderVO;
    }

    /**
     * 订单取消
     * @param id
     */
    @Transactional
    public void cancelById(Long id) {
        //1.获取传输对象
        //2.执行修改
        //①注意：如果订单状态>2则抛异常
        Orders orders = orderMapper.getById(id);
        if(orders.getStatus() > 2 ){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //②注意：如果订单是待接单则退款
        if(orders.getStatus() == 1){
            log.info("退款成功");
        }

        orders.setStatus(6);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orderMapper.update(orders);
        //3.回显对象
    }

    public void repetition(Long id) {
        //1.获取传输对象
        //2.执行添加购物车

        //2.1获取订单详细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //2.2封装购物车对象
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        orderDetailList.forEach(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCartList.add(shoppingCart);
        });

        //2.3添加购物车
        shoppingCartMapper.insertBatch(shoppingCartList);

        //3.回显对象
    }

    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //1.获取传输对象
        //2.执行查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.conditionQuery(ordersPageQueryDTO);
        List<Orders> ordersList = page.getResult();

        //①注意：封装OrderVO对象
        List<OrderVO> orderVOList = new ArrayList<>();
        if(ordersList != null && ordersList.size()>0){
            ordersList.forEach(orders -> {
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //②注意：封装详细订单字符串
                List<String> orderDishList = orderDetailList.stream().map(orderDetail -> {
                    String orderDish = orderDetail.getName() + "*" +orderDetail.getNumber();
                    return orderDish;
                }).collect(Collectors.toList());
                orderVO.setOrderDishes(String.join("",orderDishList));

                orderVOList.add(orderVO);
            });
        }

        //3.回显对象
        return new PageResult(page.getTotal(),orderVOList);
    }

    /**
     * 统计状态数量
     * @return
     */
    public OrderStatisticsVO statistics() {
        //1.获取传输对象
        //2.执行统计
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        //3.回显对象
        return orderStatisticsVO;
    }

    @Override
    public OrderVO details(Long id) {
        //1.获取传输对象
        //2.执行查找
        //注意：封装回显对象
        //2.1获取订单对象
        Orders orders = orderMapper.getById(id);
        //2.2获取订单详细对象
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        //2.3封装对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        //3.回显对象
        return orderVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //1.获取传输对象
        //2.执行查询
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(ordersConfirmDTO.getStatus())
                .build();

        orderMapper.update(orders);
        //3.回显对象
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //1.获取传输对象
        //2.执行修改

        //2.1获取订单对象
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        //2.2判断订单对象是否为待接单状态
        if(ordersDB == null || ordersDB.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //2.3如果是已付款则退款
        if(ordersDB.getPayStatus() == 1){
            log.info("申请退款");
        }
        //2.4执行动态修改
        //注意：为什么再多创建一个对象来动态修改？修改的字段越少越块
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(ordersDB.CANCELLED);
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

        //3.回显对象
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //1.获取传输对象
        //2.执行修改

        //2.1提高健壮性，避免通过postman来绕过前端   ①状态为待派送 ②如果已付款则退款
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        if(ordersDB == null || ordersDB.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(ordersDB.getPayStatus() == Orders.PAID){
            log.info("申请退款");
        }
        //2.2为了减少字段修改，新建orders对象
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

        //3.回显对象
    }

    @Override
    public void delivery(Long id) {
        //1.获取传输对象
        //2.执行修改

        //2.1健壮性 判断状态为待派送
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || ordersDB.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //2.2动态修改
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);

        //3.回显对象
    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {
        //1.获取传输对象
        //2.执行修改

        //2.1提高健壮性，避免postman绕过前端  状态为派送中
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || ordersDB.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //2.2动态修改，字段越少执行越块
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);

        //3.回显对象
    }

}
