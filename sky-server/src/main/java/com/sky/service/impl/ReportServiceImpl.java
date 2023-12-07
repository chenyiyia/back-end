package com.sky.service.impl;

import com.qiniu.util.StringUtils;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import net.bytebuddy.asm.Advice;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 导出数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1.获取数据库数据
        //1.1获取近30天的数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //1.2获取数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN),LocalDateTime.of(end,LocalTime.MAX));

        //2.通过POI将数据写入Excel文件
        //2.1获取输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //2.2通过POI创建内存Excel文件
        XSSFWorkbook xssfWorkbook = null;
        OutputStream outputStream=null;
        try {
            xssfWorkbook = new XSSFWorkbook(inputStream);

            //2.3填充数据
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
            //填充时间
            xssfSheet.getRow(1).getCell(1).setCellValue("时间："+begin+"至"+end);
            //填充营业额
            xssfSheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            //填充订单完成率
            xssfSheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            //填充新增用户数
            xssfSheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            //填充有效订单
            xssfSheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            //填充平均客单价
            xssfSheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for(int i=0;i<30;i++){
                LocalDate date = begin.plusDays(i);
                BusinessDataVO businessDataVO1 = workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));

                //填充时间
                xssfSheet.getRow(7+i).getCell(1).setCellValue("时间："+LocalDateTime.of(date,LocalTime.MIN)+"至"+LocalDateTime.of(date,LocalTime.MAX));
                //填充营业额
                xssfSheet.getRow(7+i).getCell(2).setCellValue(businessDataVO1.getTurnover());
                //填充有效订单
                xssfSheet.getRow(7+i).getCell(5).setCellValue(businessDataVO1.getValidOrderCount());
                //填充订单完成率
                xssfSheet.getRow(7+i).getCell(3).setCellValue(businessDataVO1.getOrderCompletionRate());
                //填充平均客单价
                xssfSheet.getRow(7+i).getCell(6).setCellValue(businessDataVO1.getUnitPrice());
                //填充新增用户数
                xssfSheet.getRow(7+i).getCell(4).setCellValue(businessDataVO1.getNewUsers());

            }

            //3.通过输出流将Excel文件下载到客户端浏览器
            outputStream = response.getOutputStream();
            xssfWorkbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(xssfWorkbook != null){
                try {
                    xssfWorkbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
