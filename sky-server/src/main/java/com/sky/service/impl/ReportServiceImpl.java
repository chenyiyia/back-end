package com.sky.service.impl;

import com.qiniu.util.StringUtils;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;

    /**
     * 折线图
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //1.获取传输对象
        //2.执行查询

        //2.1生成dateList对象--->生成List<LocalDate>
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate temp =begin;
        while(!temp.equals(end.plusDays(+1))){
            dateList.add(temp);
            temp = temp.plusDays(+1);
        }
        //2.2生成turnoverList对象--->生成List<>
        List<BigDecimal> turnoverList = dateList.stream().map(localDate -> {
            BigDecimal bigDecimal = orderMapper.getSumAmoutByOrderTime(localDate);
            return bigDecimal == null? new BigDecimal(0) : bigDecimal;
        }).collect(Collectors.toList());

        //3.回显对象
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //1.获取传输对象
        //2.执行查找

        //2.1生成dateList对象--->生成List<LocalDate>
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate temp = begin;
        while(!temp.equals(end.plusDays(+1))){
            dateList.add(temp);
            temp = temp.plusDays(+1);
        }
        //2.2生成totalUserList对象--->生成List<Long>
        List<Long> totalUserList = dateList.stream().map(localDate -> {
            Long totalUser = orderMapper.getSumUserByOrderTime(localDate);
            return totalUser == null? 0 : totalUser;
        }).collect(Collectors.toList());
        //2.3生成newUserList对象--->生成List<Long>
        List<Long> newUserList = dateList.stream().map(localDate -> {
            Long totalNewUser = userMapper.getSumNewUserByOrderTime(localDate);
            return totalNewUser == null? 0 :totalNewUser;
        }).collect(Collectors.toList());

        //3.回显对象
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

}
