/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 项目主入口
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * BookNexus 图书管理系统 — 应用主入口
 * <p>启用特性：</p>
 * <ul>
 *   <li>Spring Boot 自动配置</li>
 *   <li>AOP 代理（@EnableAspectJAutoProxy）</li>
 *   <li>MyBatis-Plus Mapper 扫描</li>
 * </ul>
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@MapperScan("com.zjw.booknexus.mapper")
public class BookNexusApplication {

    /**
     * 启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BookNexusApplication.class, args);
    }

}
