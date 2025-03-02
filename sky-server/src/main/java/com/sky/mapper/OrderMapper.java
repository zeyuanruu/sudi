package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    Orders getById(Long id);

    void update(Orders orders);

    /**
     * 根据条件来统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);
}
