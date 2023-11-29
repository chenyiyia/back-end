package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="sky.qiniuyun")
@Data
public class QiniuyunOssProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
}
