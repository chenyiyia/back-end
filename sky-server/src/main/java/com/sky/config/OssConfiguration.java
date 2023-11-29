package com.sky.config;

import com.sky.properties.QiniuyunOssProperties;
import com.sky.utils.QiniuyunOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 配置类，用于创建QiniuyunOssUtil对象
 */
@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public QiniuyunOssUtil qiniuyunOssUtil(QiniuyunOssProperties qiniuyunOssProperties){
        log.info("开始创建阿里云文件上传工具类：{}",qiniuyunOssProperties);
        return new QiniuyunOssUtil(qiniuyunOssProperties.getAccessKey(),
                qiniuyunOssProperties.getSecretKey(),
                qiniuyunOssProperties.getBucket());
    }
}
