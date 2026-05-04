package com.zjw.booknexus.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zjw.booknexus.interceptor.LoginInterceptor;
import com.zjw.booknexus.interceptor.RequestIdInterceptor;
import com.zjw.booknexus.utils.JwtUtils;
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
 * <p>配置 Spring MVC 的跨域访问（CORS）、拦截器注册、JSON 序列化等全局行为。</p>
 *
 * <p><b>配置项：</b></p>
 * <ul>
 *   <li><b>CORS</b> — 允许所有来源跨域访问 {@code /api/**} 路径，支持凭证传递</li>
 *   <li><b>拦截器</b> — 注册 RequestIdInterceptor（请求追踪）和 LoginInterceptor（JWT 鉴权）</li>
 *   <li><b>Jackson 序列化</b> — 日期格式 {@code yyyy-MM-dd HH:mm:ss}、时区 Asia/Shanghai、
 *   忽略未知属性、不序列化 null 值</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * JWT 工具类，用于登录拦截器中解析与校验 Token
     */
    private final JwtUtils jwtUtils;

    /**
     * 构造注入 JwtUtils
     *
     * @param jwtUtils JWT 令牌工具类
     */
    public WebMvcConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 配置跨域资源共享（CORS）
     * <p>允许前端应用（Vite 开发服务器端口 90）跨域访问后端 API。
     * 支持凭证（Cookies/Authorization 头）传递，预检请求缓存 1 小时。</p>
     *
     * @param registry CORS 配置注册器
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
     * 注册拦截器
     * <p>按以下顺序注册拦截器：</p>
     * <ol>
     *   <li><b>RequestIdInterceptor</b> — 为每个请求注入唯一追踪 ID（UUID），
     *   写入 MDC 用于日志链路追踪。拦截路径：{@code /api/**}</li>
     *   <li><b>LoginInterceptor</b> — JWT Token 校验，拦截管理端与用户端 API，
     *   放行公开接口 {@code /api/v1/public/**}</li>
     * </ol>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestIdInterceptor())
                .addPathPatterns("/api/**");

        registry.addInterceptor(new LoginInterceptor(jwtUtils))
                .addPathPatterns("/api/v1/admin/**", "/api/v1/user/**")
                .excludePathPatterns("/api/v1/public/**");
    }

    /**
     * 配置 Jackson ObjectMapper 全局序列化行为
     * <p>覆盖 Spring Boot 默认 Jackson 配置，确保所有 API 响应的
     * JSON 序列化行为一致：</p>
     * <ul>
     *   <li>日期格式：{@code yyyy-MM-dd HH:mm:ss}</li>
     *   <li>时区：Asia/Shanghai（北京时间，UTC+8）</li>
     *   <li>忽略未知属性：反序列化时跳过 Java 对象中不存在的字段，避免抛出异常</li>
     *   <li>不序列化 null 值：减少响应体积，前端无需处理空字段</li>
     * </ul>
     *
     * @return Jackson2ObjectMapperBuilder 构建器
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
