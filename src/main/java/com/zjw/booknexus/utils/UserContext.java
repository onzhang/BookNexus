package com.zjw.booknexus.utils;

/**
 * 用户上下文持有者
 * <p>基于 {@link ThreadLocal} 实现当前请求线程的用户信息持有。
 * 由 {@link com.zjw.booknexus.interceptor.LoginInterceptor} 注入，
 * 由 {@link com.zjw.booknexus.config.UserContextFilter} 清理。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
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

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}