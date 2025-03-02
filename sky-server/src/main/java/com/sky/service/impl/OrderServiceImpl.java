package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
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
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sky.entity.OrderDetail;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.sky.vo.OrderSubmitVO;
import org.springframework.transaction.annotation.Transactional;

import static com.sky.entity.Orders.TO_BE_CONFIRMED;

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
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //0.处理各种业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前用户购物车信息
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> ShoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(ShoppingCartList == null || ShoppingCartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //1.向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        OrderDetail orderDetail = null;
        //2.向订单详细表插入n条数据
        for (ShoppingCart cart : ShoppingCartList) {
            orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //3.清空当前购物车数据
        shoppingCartMapper.clean(userId);
        //4.封装VO返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    @Override
    public void userCancelById(Long id) {
        //1.查询要退款的订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            //1.1 不存在，返回异常
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //1.2 存在，继续下一个判断，判断如果订单状态大于2，那也抛异常，不能取消订单
        if(orders.getStatus() > 2 ){
            //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //2. 订单处于待接单状态下取消需要退款，此时不做退款处理（没有微信支付权限），只做输出，修改支付状态
        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            log.info("处在待接单状态下，用户取消订单，进行退款...");

        }
        //3.更新订单状态
        orders.setPayStatus(Orders.REFUND);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
//    public PageResult pageQueryForUser(int page, int pageSize, Integer status) {
//        orderDetailMapper.pageQuery(page,)
//        return null;
//    }
}
