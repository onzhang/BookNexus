package com.zjw.booknexus;

import com.zjw.booknexus.interceptor.LoginInterceptor;
import com.zjw.booknexus.interceptor.RequestIdInterceptor;
import com.zjw.booknexus.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 测试配置类，用于 @WebMvcTest 集成测试中注册与生产环境一致的拦截器。
 */
@TestConfiguration
public class MvcTestConfig implements WebMvcConfigurer {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestIdInterceptor())
                .addPathPatterns("/api/**");

        registry.addInterceptor(new LoginInterceptor(jwtUtils))
                .addPathPatterns("/api/v1/admin/**", "/api/v1/user/**")
                .excludePathPatterns("/api/v1/public/**");
    }
}
