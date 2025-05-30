package com.sky.utils;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.sky.config.QiniuConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class QiniuUtil {

    @Autowired
    private QiniuConfig qiniuConfig;

    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问路径
     */
    public String upload(MultipartFile file) {
        try {
            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成新的文件名
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            
            // 生成上传凭证
            Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
            String upToken = auth.uploadToken(qiniuConfig.getBucket());
            
            // 创建上传对象
            Configuration cfg = new Configuration(Region.autoRegion());
            UploadManager uploadManager = new UploadManager(cfg);
            
            // 上传文件
            Response response = uploadManager.put(inputStream, fileName, upToken, null, null);
            
            // 检查上传结果
            if (!response.isOK()) {
                log.error("文件上传失败，错误信息：{}", response.error);
                throw new RuntimeException("文件上传失败：" + response.error);
            }
            
            // 处理域名，确保以http://或https://开头，且末尾没有斜杠
            String domain = qiniuConfig.getDomain();
            if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                domain = "http://" + domain;
            }
            if (domain.endsWith("/")) {
                domain = domain.substring(0, domain.length() - 1);
            }
            
            // 返回文件访问路径
            return domain + "/" + fileName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }
} 