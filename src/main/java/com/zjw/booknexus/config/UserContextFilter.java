package com.zjw.booknexus.config;

import com.zjw.booknexus.utils.UserContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * <p>负责在请求处理结束后清理 {@link UserContext} 的 ThreadLocal 变量，
 * 防止内存泄漏。用户信息的注入由 {@link com.zjw.booknexus.interceptor.LoginInterceptor}
 * 在 HandlerInterceptor 层完成。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Component
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}