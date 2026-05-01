/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * MinIO 对象存储客户端配置（书籍封面图等文件存储）
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置
 * <p>用于书籍封面图等文件的上传与访问，替代本地磁盘存储，
 * 为后续集群部署提供统一的文件存储层。</p>
 */
@Slf4j
@Configuration
public class MinIOConfig {

    /** MinIO 服务端点地址 */
    @Value("${minio.endpoint}")
    private String endpoint;

    /** 访问密钥 */
    @Value("${minio.access-key}")
    private String accessKey;

    /** 秘密密钥 */
    @Value("${minio.secret-key}")
    private String secretKey;

    /** 默认存储桶名称 */
    @Value("${minio.bucket}")
    private String bucket;

    /**
     * MinIO 客户端
     *
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 获取默认存储桶名称
     *
     * @return bucket name
     */
    public String getBucket() { return bucket; }

    /**
     * 初始化检查并创建存储桶
     * <p>应用启动时自动检查 bucket 是否存在，不存在则自动创建。</p>
     */
    @PostConstruct
    public void init() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket '{}' created successfully", bucket);
            } else {
                log.info("MinIO bucket '{}' already exists", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket '{}'", bucket, e);
        }
    }

}
