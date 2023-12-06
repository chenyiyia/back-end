package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.vo.TurnoverReportVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/turnover")
    Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){

    }

}
