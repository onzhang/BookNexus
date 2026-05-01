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
 * <p>Spring Boot 启动类，负责完成以下初始化工作：</p>
 * <ul>
 *   <li>Spring Boot 自动配置 — 根据 classpath 依赖自动装配各组件</li>
 *   <li>AOP 代理 — {@code @EnableAspectJAutoProxy} 启用 AspectJ 注解支持</li>
 *   <li>Mapper 扫描 — {@code @MapperScan} 扫描 MyBatis-Plus Mapper 接口</li>
 * </ul>
 *
 * <p>构建命令：</p>
 * <pre>
 * mvn clean compile          # 编译
 * mvn spring-boot:run        # 运行（dev profile）
 * mvn clean package -Pprod   # 打包
 * </pre>
 *
 * @author 张俊文
 * @since 2026-04-30
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
