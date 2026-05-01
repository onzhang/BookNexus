/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 登录认证拦截器，校验请求头中的 JWT Token 有效性
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 * <p>拦截 /api/v1/admin/** 和 /api/v1/user/** 请求，从请求头中提取 JWT Token
 * 并校验其有效性，校验失败则返回 401 响应。</p>
 *
 * <h3>M3 阶段实现计划：</h3>
 * <ol>
 *   <li><b>Token 提取</b> — 从请求头获取 {@code Authorization} 值，校验 Bearer 前缀</li>
 *   <li><b>Token 解析</b> — 使用 jjwt 库
 *       {@code Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)}
 *       解析 JWT，获取 Claims 中的用户信息（userId、role）</li>
 *   <li><b>角色校验</b> — 根据请求路径判断角色：
 *       {@code /api/v1/admin/**} 需 {@code admin} 角色，
 *       {@code /api/v1/user/**} 需 {@code user} 或 {@code admin} 角色</li>
 *   <li><b>Token 过期检查</b> — 校验 Claims 中的 {@code exp} 字段，过期返回 401</li>
 *   <li><b>放行／拒绝</b> — 校验通过则将 {@code userId} 存入
 *       {@code request.setAttribute("userId", ...)} 供 Controller 使用；
 *       校验失败返回 401 JSON 响应</li>
 * </ol>
 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 请求前置处理 — M3 阶段实现 JWT 验证
     *
     * <p>实现步骤：</p>
     * <pre>
     * 1. Token 提取
     *    String authHeader = request.getHeader("Authorization");
     *    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
     *        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
     *        response.setContentType("application/json;charset=UTF-8");
     *        response.getWriter().write("{\"code\":401,\"message\":\"unauthorized\"}");
     *        return false;
     *    }
     *    String token = authHeader.substring(7);
     *
     * 2. Token 解析（jjwt）
     *    try {
     *        Claims claims = Jwts.parser()
     *                .verifyWith(SECRET_KEY)
     *                .build()
     *                .parseSignedClaims(token)
     *                .getPayload();
     *        Long userId = claims.get("userId", Long.class);
     *        String role = claims.get("role", String.class);
     *
     * 3. 角色校验
     *        String path = request.getRequestURI();
     *        if (path.startsWith("/api/v1/admin/") && !"admin".equals(role)) {
     *            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
     *            response.setContentType("application/json;charset=UTF-8");
     *            response.getWriter().write("{\"code\":403,\"message\":\"forbidden\"}");
     *            return false;
     *        }
     *
     * 4. 放行 — 存入请求属性
     *        request.setAttribute("userId", userId);
     *        request.setAttribute("role", role);
     *    } catch (JwtException e) {
     *        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
     *        response.setContentType("application/json;charset=UTF-8");
     *        response.getWriter().write("{\"code\":401,\"message\":\"invalid token\"}");
     *        return false;
     *    }
     * </pre>
     *
     * @param request  当前 HTTP 请求
     * @param response HTTP 响应
     * @param handler  请求处理器
     * @return true=放行，false=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // TODO: M3 阶段实现 JWT Token 解析与校验（参见类注释实现步骤）
        return true;
    }

}
