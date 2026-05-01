/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Web MVC 配置：跨域资源共享（CORS）、登录拦截器注册
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zjw.booknexus.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Web MVC 配置
 * <p>配置 CORS 跨域策略和 API 登录拦截规则。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置跨域资源共享
     * <p>允许所有来源的跨域请求（开发环境），
     * 生产环境建议收紧 allowedOriginPatterns。</p>
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册登录拦截器
     * <p>拦截 /api/v1/admin/** 和 /api/v1/user/** 路径，
     * 放行 /api/v1/public/** 公开接口。</p>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/v1/admin/**", "/api/v1/user/**")
                .excludePathPatterns("/api/v1/public/**");
    }

    /**
     * Jackson 消息转换器
     * <p>显式声明 Jackson2ObjectMapperBuilder Bean，确保消息转换器配置可控。</p>
     *
     * @return Jackson2ObjectMapperBuilder
     */
    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .timeZone(TimeZone.getTimeZone("Asia/Shanghai"))
                .failOnUnknownProperties(false)
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
