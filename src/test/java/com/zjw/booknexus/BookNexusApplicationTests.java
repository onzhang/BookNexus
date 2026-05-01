/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 应用上下文加载测试
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用上下文加载测试
 * <p>校验 Spring 容器能否正常启动，所有 Bean 能否正确注入。
 * 使用 dev profile 加载开发配置中间件地址。</p>
 */
@SpringBootTest
@ActiveProfiles("dev")
class BookNexusApplicationTests {

    /**
     * 验证 Spring 上下文加载成功
     * <p>如果应用配置或 Bean 注入存在问题，此测试会失败。</p>
     */
    @Test
    void contextLoads() {
    }

}
