/**
 * JWT 登录拦截器
 * <p>基于 {@link HandlerInterceptor} 实现对 API 请求的认证与授权。
 * 拦截所有非 OPTIONS 请求，验证请求头中的 Bearer Token。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>Token 解析与校验 — 调用 {@link JwtUtils#parseToken(String)} 解析 JWT，验证签名和过期时间</li>
 *   <li>Token 类型校验 — 仅接受 {@code type=access} 的 Access Token，拒绝 Refresh Token</li>
 *   <li>管理端权限校验 — {@code /api/v1/admin/**} 路径仅允许 ADMIN 角色访问</li>
 *   <li>用户信息注入 — 将 userId 和 role 写入 request attribute，供后续业务层使用</li>
 * </ul>
 *
 * <p>校验失败时返回统一格式的 JSON 响应（使用 {@link Result} 类），包含 requestId 和 timestamp 字段。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.utils.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public LoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 从请求头中提取 Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResult(response, 401, Result.unauthorized("unauthorized"));
            return false;
        }

        // 3. 截取 Bearer 后的实际 Token 字符串
        String token = authHeader.substring(7);

        try {
            // 4. 解析 JWT：验证签名和过期时间
            Claims claims = jwtUtils.parseToken(token);

            // 5. 校验令牌类型：仅接受 type=access 的 Access Token
            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                sendResult(response, 401, Result.unauthorized("invalid token type"));
                return false;
            }

            // 6. 提取用户身份信息
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            // 7. 管理端权限校验：admin 路径仅限 ADMIN 角色访问
            String path = request.getRequestURI();
            if (path.startsWith("/api/v1/admin/") && !"ADMIN".equals(role)) {
                sendResult(response, 403, Result.forbidden("admin access required"));
                return false;
            }

            // 8. 将用户信息注入 UserContext（ThreadLocal），供 Service 层直接获取
            UserContext.set(userId, role);
        } catch (JwtException e) {
            // 9. 令牌解析失败（签名无效或已过期），返回 401
            sendResult(response, 401, Result.unauthorized("invalid or expired token"));
            return false;
        }

        // 10. 校验通过，放行请求
        return true;
    }

    /**
     * 发送统一格式的 JSON 错误响应，使用 {@link Result} 类确保响应包含 requestId 和 timestamp 字段。
     *
     * @param response HTTP 响应对象
     * @param status   HTTP 状态码
     * @param result   Result 对象
     * @throws Exception 写入响应时可能抛出的异常
     */
    private void sendResult(HttpServletResponse response, int status, Result<?> result) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), result);
    }
}
