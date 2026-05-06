/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * MyBatis-Plus 配置：分页插件、Mapper 扫描
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * MyBatis-Plus 配置
 * <p>注册 MyBatis-Plus 扩展插件，包括分页插件与乐观锁插件。
 * 所有 Mapper 接口继承 BaseMapper 后自动获得分页查询能力，
 * 实体类字段标注 {@code @Version} 注解后可自动实现乐观锁机制。</p>
 *
 * <p><b>涉及框架：</b>MyBatis-Plus 3.5.9 + mybatis-plus-join 1.4.x</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
@MapperScan(basePackages = "com.zjw.booknexus.mapper",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*es\\..*"))
public class MyBatisPlusConfig {

    /**
     * 创建 MyBatis-Plus 拦截器链
     * <p>注册以下内置拦截器：</p>
     * <ul>
     *   <li><b>PaginationInnerInterceptor</b> — MySQL 分页方言，支持 Page 对象
     *   自动拼接 LIMIT 语句，无需手动编写分页 SQL</li>
     *   <li><b>OptimisticLockerInnerInterceptor</b> — 乐观锁插件，更新时自动检查
     *   {@code @Version} 字段版本号，避免并发写冲突</li>
     * </ul>
     *
     * @return MybatisPlusInterceptor 拦截器链，依次执行各内置插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

}
