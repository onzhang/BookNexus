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
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * <p>注册分页插件，所有 Mapper 继承 BaseMapper 后自动获得分页能力。</p>
 */
@Configuration
@MapperScan("com.zjw.booknexus.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器链
     * <p>添加 MySQL 分页插件，支持 Page 对象分页查询。
     * 后续可扩展乐观锁、SQL 性能分析等插件。</p>
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

}
