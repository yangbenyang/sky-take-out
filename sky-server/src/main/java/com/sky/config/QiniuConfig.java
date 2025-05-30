package com.sky.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sky.qiniu")
public class QiniuConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;
} 