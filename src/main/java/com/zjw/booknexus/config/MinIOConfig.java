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
 *
 * <p><b>涉及中间件：</b>MinIO 对象存储服务</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Configuration
public class MinIOConfig {

    /**
     * MinIO 服务端点地址
     * <p>格式：{@code http://host:port}，从配置项 {@code minio.endpoint} 注入。</p>
     */
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * MinIO 访问密钥（Access Key）
     * <p>用于身份认证，对应 MinIO 控制台生成的 AccessKey。</p>
     */
    @Value("${minio.access-key}")
    private String accessKey;

    /**
     * MinIO 秘密密钥（Secret Key）
     * <p>用于身份认证，对应 MinIO 控制台生成的 SecretKey，需妥善保管。</p>
     */
    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * 默认存储桶名称
     * <p>书籍封面图等文件存储的目标桶，从配置项 {@code minio.bucket} 注入。</p>
     */
    @Value("${minio.bucket}")
    private String bucket;

    /**
     * 创建 MinIO 客户端
     * <p>基于配置的端点地址与访问凭证构建 MinIO 客户端实例，用于执行
     * 存储桶与对象的上传、下载、删除等操作。该客户端为线程安全单例。</p>
     *
     * @return MinioClient 实例，封装了与 MinIO 服务交互的所有 API
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
     * <p>供 Service 层在文件上传时调用，确定文件存储的目标桶。</p>
     *
     * @return 默认存储桶名称字符串
     */
    public String getBucket() { return bucket; }

    /**
     * 初始化检查并创建存储桶
     * <p>应用启动时自动检查配置的默认存储桶是否存在，若不存在则自动创建。
     * 确保文件上传操作前目标存储桶已就绪，避免因缺失存储桶导致上传失败。
     * 若初始化过程发生异常（如 MinIO 服务不可达），仅记录错误日志不阻断启动。</p>
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
