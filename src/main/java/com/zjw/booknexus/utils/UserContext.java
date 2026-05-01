package com.zjw.booknexus.utils;

/**
 * 用户上下文持有者
 * <p>基于 {@link ThreadLocal} 实现当前请求线程的用户信息持有，
 * 在 Filter 层（{@link com.zjw.booknexus.config.UserContextFilter UserContextFilter}）进行设置和清理，
 * Service 层直接通过静态方法获取当前登录用户信息，避免在方法签名中频繁传递 userId 和 role。</p>
 *
 * <p>使用规范：</p>
 * <ul>
 *   <li>设置 — 由 {@code UserContextFilter} 在请求入口自动完成</li>
 *   <li>获取 — 业务代码通过 {@link #getUserId()} / {@link #getRole()} / {@link #isAdmin()} 获取</li>
 *   <li>清理 — 由 {@code UserContextFilter} 在 finally 块中自动清理</li>
 *   <li>禁止 — 业务代码中手动调用 {@link #set(Long, String)}，以免破坏上下文一致性</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */

import jakarta.servlet.http.HttpServletRequest;

public class UserContext {

    /** 当前线程的用户 ID */
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    /** 当前线程的用户角色 */
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    /**
     * 设置当前线程的用户上下文
     *
     * @param userId 用户 ID
     * @param role   用户角色
     */
    public static void set(Long userId, String role) {
        userIdHolder.set(userId);
        roleHolder.set(role);
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未登录时返回 null
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }

    /**
     * 获取当前登录用户角色
     *
     * @return 用户角色，未登录时返回 null
     */
    public static String getRole() {
        return roleHolder.get();
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return true 如果当前用户角色为 ADMIN
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(roleHolder.get());
    }

    /**
     * 从 HttpServletRequest 属性中设置用户上下文
     * <p>由 {@link com.zjw.booknexus.config.UserContextFilter UserContextFilter} 调用，
     * 读取 LoginInterceptor 注入到 request attribute 中的用户信息。</p>
     *
     * @param request HTTP 请求对象
     */
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

    /**
     * 清理当前线程的用户上下文
     * <p>必须由 {@link com.zjw.booknexus.config.UserContextFilter UserContextFilter}
     * 在请求处理结束后的 finally 块中调用，防止 ThreadLocal 内存泄漏。</p>
     */
    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}
