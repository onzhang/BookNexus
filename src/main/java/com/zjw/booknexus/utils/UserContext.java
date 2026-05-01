package com.zjw.booknexus.utils;

import jakarta.servlet.http.HttpServletRequest;

public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    public static void set(Long userId, String role) {
        userIdHolder.set(userId);
        roleHolder.set(role);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static String getRole() {
        return roleHolder.get();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(roleHolder.get());
    }

    public static void setFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        Object role = request.getAttribute("role");
        if (userId != null) {
            userIdHolder.set((Long) userId);
        }
        if (role != null) {
            roleHolder.set((String) role);
        }
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}
