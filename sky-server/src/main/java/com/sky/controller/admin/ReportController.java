package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api("数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    ReportService reportService;

    /**
     * 折线图
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("折线图接口")
    Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //1.获取传输对象
        log.info("营业额数据统计：{},{}",begin,end);
        //2.执行查找
        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(begin,end);
        //3.回显对象
        return Result.success(turnoverReportVO);
    }

    @GetMapping("/userStatistics")
    Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        //1.获取传输对象
        log.info("用户数统计：{},{}",begin,end);
        //2.执行查找
        UserReportVO userReportVO = reportService.getUserStatistics(begin,end);
        //3.回显对象
        return Result.success(userReportVO);
    }

}
