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
 * <p>注册 XXL-Job 执行器 Bean，连接调度中心并执行定时任务。</p>
 *
 * <p><b>业务场景：</b></p>
 * <ul>
 *   <li><b>借阅逾期检查</b> — 每天 8:00 扫描超期未还书籍，生成逾期记录并推送催还通知</li>
 *   <li><b>热门排行同步</b> — 每小时从 Redis SortedSet 同步借阅排行榜数据到数据库</li>
 *   <li><b>缓存预热</b> — 每日凌晨预加载热门书籍数据到 Redis 缓存，降低上班高峰期 DB 压力</li>
 * </ul>
 *
 * <p><b>特性：</b></p>
 * <ul>
 *   <li>空地址保护：未配置调度中心地址时不会注册执行器，本地开发自动跳过</li>
 *   <li>条件注入：使用 {@code @ConditionalOnExpression} 控制 Bean 注册时机</li>
 * </ul>
 *
 * <p><b>涉及中间件：</b>XXL-Job 2.4.1（分布式调度框架）</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
public class XXLJobConfig {

    /**
     * XXL-Job 调度中心部署地址
     * <p>多个地址逗号分隔，如：{@code http://192.168.100.128:8080/xxl-job-admin}。
     * 若未配置则为空字符串，执行器不会注册（用于本地开发环境）。</p>
     */
    @Value("${xxl.job.admin-addresses:}")
    private String adminAddresses;

    /**
     * 执行器应用名称
     * <p>用于在调度中心标识该执行器，需与调度中心新建执行器时配置的名称一致。</p>
     */
    @Value("${xxl.job.app-name:booknexus-job}")
    private String appName;

    /**
     * 执行器 RPC 端口
     * <p>执行器与调度中心之间通信的端口号，默认 9999，避免与业务端口冲突。</p>
     */
    @Value("${xxl.job.port:9999}")
    private int port;

    /**
     * 执行器日志存储路径
     * <p>任务执行日志的本地磁盘存储目录，默认路径：{@code /data/applogs/xxl-job}。</p>
     */
    @Value("${xxl.job.log-path:/data/applogs/xxl-job}")
    private String logPath;

    /**
     * 创建并注册 XXL-Job 执行器
     * <p>配置执行器与调度中心通信所需的地址、端口及应用名信息。
     * 通过 {@code @ConditionalOnExpression} 控制 Bean 注册时机：
     * 仅在 {@code xxl.job.admin-addresses} 配置项非空时才会创建 Bean，
     * 避免本地开发时因调度中心不可用导致启动报错。</p>
     *
     * <p>执行器注册成功后，可在 XXL-Job 调度中心 Web 界面中创建任务
     * 并指定该执行器运行。</p>
     *
     * @return XxlJobSpringExecutor 实例，管理任务的生命周期与调度执行
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
