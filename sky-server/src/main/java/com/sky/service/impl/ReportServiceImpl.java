package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 统计指定时间内区间的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList();
        for (LocalDate date = begin; date.isBefore(end); date = date.plusDays(1)) {
            dateList.add(date);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from orders where status = 5 and order_time >beginTime and order_time < endTime
            Map map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        String date = StringUtils.join(dateList, ",");
        String turnover = StringUtils.join(turnoverList, ",");
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .turnoverList(turnover)
                .dateList(date)
                .build();
        return turnoverReportVO;
    }
    /**
     * 统计指定时间内区间的用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList();
        for(LocalDate date = begin; date.isBefore(end);date = date.plusDays(1)){
            dateList.add(date);
        }
        List<Integer> userList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("end",endTime);
            int totalCount = userMapper.countByMap(map);
            map.put("begin",beginTime);
            int count = userMapper.countByMap(map);
            userList.add(count);
            totalUserList.add(totalCount);
        }
        String date = StringUtils.join(dateList, ",");
        String newUser = StringUtils.join(userList, ",");
        String totalUser = StringUtils.join(totalUserList, ",");
        // select count(id) from user where create_time < endTime and create_time > beginTime
        // select count(id) from user where create_time < endTime
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(date)
                .newUserList(newUser)
                .totalUserList(totalUser)
                .build();
        return userReportVO;
    }
}
