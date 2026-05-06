/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Sentinel 限流熔断配置：开启 @SentinelResource 注解支持
 * 并注册全局限流与降级规则
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
 * <p>注册 Sentinel 注解 AOP 切面，启用 {@code @SentinelResource} 注解支持。
 * 限流与降级规则由 {@link com.zjw.booknexus.sentinel.SentinelRuleInitializer} 在应用启动时自动初始化。</p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * @SentinelResource(value = "borrowBook", fallback = "borrowFallback", fallbackClass = SentinelRuleInitializer.class)
 * public Result borrowBook(BorrowReq req) { ... }
 * }</pre>
 *
 * <p><b>涉及中间件：</b>Sentinel 1.8.8（限流熔断框架）</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
public class SentinelConfig {

    /**
     * 创建 Sentinel 注解 AOP 切面
     * <p>注册 {@link SentinelResourceAspect} 到 Spring 容器，解析带 {@code @SentinelResource}
     * 注解的方法，拦截调用并执行限流/熔断/降级判定。当流量超过阈值时自动触发降级逻辑，
     * 保护下游服务不被突发流量击穿。</p>
     *
     * @return SentinelResourceAspect 切面实例，启用 Sentinel 注解能力
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

}
