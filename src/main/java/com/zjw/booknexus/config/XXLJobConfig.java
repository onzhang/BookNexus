/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * XXL-Job 分布式定时任务执行器配置
 * 业务场景：借阅逾期检查、热门排行同步、缓存预热
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job 分布式定时任务执行器配置
 * <p>业务场景：</p>
 * <ul>
 *   <li>借阅逾期检查 — 每天 8:00 检查超期未还书籍</li>
 *   <li>热门排行同步 — 每小时从 Redis SortedSet 同步到 DB</li>
 *   <li>缓存预热 — 每日凌晨预加载热门书籍到缓存</li>
 * </ul>
 * <p>未配置调度中心地址时不会注册执行器，可本地开发跳过。</p>
 */
@Configuration
public class XXLJobConfig {

    /** XXL-Job 调度中心地址（空则不启用） */
    @Value("${xxl.job.admin-addresses:}")
    private String adminAddresses;

    /** 执行器应用名 */
    @Value("${xxl.job.app-name:booknexus-job}")
    private String appName;

    /** 执行器 RPC 端口 */
    @Value("${xxl.job.port:9999}")
    private int port;

    /** 执行器日志路径 */
    @Value("${xxl.job.log-path:/data/applogs/xxl-job}")
    private String logPath;

    /**
     * 注册 XXL-Job 执行器
     * <p>通过 @ConditionalOnExpression 控制：仅当 admin-addresses 非空时注册 Bean。</p>
     *
     * @return XxlJobSpringExecutor
     */
    @Bean
    @ConditionalOnExpression("'${xxl.job.admin-addresses:}' != ''")
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAppname(appName);
        executor.setPort(port);
        executor.setLogPath(logPath);
        return executor;
    }

}
