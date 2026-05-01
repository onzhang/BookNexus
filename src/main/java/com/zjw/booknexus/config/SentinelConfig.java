/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Sentinel 限流熔断配置：开启 @SentinelResource 注解支持
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 限流熔断配置
 * <p>注册 SentinelResourceAspect 切面，启用 @SentinelResource 注解，
 * 可在 Service 方法上定义限流、熔断、降级规则。</p>
 */
@Configuration
public class SentinelConfig {

    /**
     * Sentinel 注解 AOP 切面
     * <p>开启后可在方法上使用 @SentinelResource(value = "resourceName", fallback = "xxx")。</p>
     *
     * @return SentinelResourceAspect
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

}
