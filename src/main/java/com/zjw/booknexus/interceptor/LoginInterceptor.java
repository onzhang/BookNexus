/**
 * JWT 登录拦截器
 * <p>基于 {@link HandlerInterceptor} 实现对 API 请求的认证与授权。
 * 拦截所有非 OPTIONS 请求，验证请求头中的 Bearer Token，</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>Token 解析与校验 — 调用 {@link JwtUtils#parseToken(String)} 解析 JWT，验证签名和过期时间</li>
 *   <li>Token 类型校验 — 仅接受 {@code type=access} 的 Access Token，拒绝 Refresh Token</li>
 *   <li>管理端权限校验 — {@code /api/v1/admin/**} 路径仅允许 ADMIN 角色访问</li>
 *   <li>用户信息注入 — 将 userId 和 role 写入 request attribute，供后续业务层使用</li>
 * </ul>
 *
 * <p>校验失败时直接返回 JSON 响应（401/403），不继续执行请求链。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.interceptor;

import com.zjw.booknexus.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    /** JWT 工具类，提供 Token 解析能力 */
    private final JwtUtils jwtUtils;

    /**
     * 构造登录拦截器
     *
     * @param jwtUtils JWT 工具类，用于 Token 解析与校验
     */
    public LoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 前置拦截 — 执行认证与授权校验
     * <p>处理流程：OPTIONS 预检请求直接放行 → 提取 Authorization 头 → 解析并校验 Token →
     * 校验 Token 类型 → 校验管理端权限 → 注入用户信息到请求上下文。</p>
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  被调用的处理器对象
     * @return true 放行请求，false 拦截请求并返回错误响应
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJson(response, 401, "{\"code\":401,\"message\":\"unauthorized\"}");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtils.parseToken(token);

            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                sendJson(response, 401, "{\"code\":401,\"message\":\"invalid token type\"}");
                return false;
            }

            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            String path = request.getRequestURI();
            if (path.startsWith("/api/v1/admin/") && !"ADMIN".equals(role)) {
                sendJson(response, 403, "{\"code\":403,\"message\":\"admin access required\"}");
                return false;
            }

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
        } catch (JwtException e) {
            sendJson(response, 401, "{\"code\":401,\"message\":\"invalid or expired token\"}");
            return false;
        }

        return true;
    }

    /**
     * 发送 JSON 格式的错误响应
     *
     * @param response HTTP 响应对象
     * @param status   HTTP 状态码
     * @param body     JSON 响应体字符串
     * @throws Exception 写入响应时可能抛出的异常
     */
    private void sendJson(HttpServletResponse response, int status, String body) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}
