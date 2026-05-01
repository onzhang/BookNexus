package com.zjw.booknexus.interceptor;

import com.zjw.booknexus.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public LoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

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

    private void sendJson(HttpServletResponse response, int status, String body) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}
