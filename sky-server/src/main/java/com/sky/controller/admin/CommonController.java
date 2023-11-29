package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.QiniuyunOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping(value="/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    QiniuyunOssUtil qiniuyunOssUtil;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping(value = "/upload")
    @ApiOperation(value = "文件上传")
    Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);

        //设置文件名
        String originalFilename=file.getOriginalFilename();
        String postfix=originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName= UUID.randomUUID()+postfix;

        try {
            String url=qiniuyunOssUtil.upload(file.getBytes(),objectName);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败 {}",e);
        }
        return Result.error("文件上传失败");
    }
}
