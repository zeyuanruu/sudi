package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitVO);

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
//    PageResult pageQueryForUser(int page, int pageSize, Integer status);

    void userCancelById(Long id);
}
