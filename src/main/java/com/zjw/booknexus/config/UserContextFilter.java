/**
 * 用户上下文过滤器
 * <p>在请求进入拦截器链之前，将 LoginInterceptor 注入到 request attribute 中的
 * 用户信息（userId / role）同步到 {@link UserContext} 线程本地变量中，
 * 使得 Service 层和各工具类可以通过 UserContext 静态方法获取当前用户信息。</p>
 *
 * <p>设计说明：</p>
 * <ul>
 *   <li>使用 {@link jakarta.servlet.Filter Filter}（优先于 Interceptor）确保用户信息尽早注入</li>
 *   <li>请求处理结束后在 {@code finally} 块中调用 {@link UserContext#clear()} 清理线程变量</li>
 *   <li>对于非 {@link HttpServletRequest} 的请求（如 forward 内部请求），自动跳过设置</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.config;

import com.zjw.booknexus.utils.UserContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserContextFilter implements Filter {

    /**
     * 执行过滤 — 将请求上下文中的用户信息注入到当前线程
     * <p>从 request attribute 中读取 userId 和 role 并设置到 UserContext，
     * 处理完成后确保清理线程变量，防止内存泄漏。</p>
     *
     * @param request  Servlet 请求对象
     * @param response Servlet 响应对象
     * @param chain    过滤器链
     * @throws IOException      I/O 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest httpRequest) {
                UserContext.setFromRequest(httpRequest);
            }
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
