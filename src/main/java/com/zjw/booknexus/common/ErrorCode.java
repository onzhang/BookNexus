/**
 * 业务错误码常量定义
 * <p>集中管理系统所有业务场景的错误码，采用字符串枚举风格（如 {@code BOOK_NOT_FOUND}），
 * 避免与 HTTP 状态码混淆，同时具备可读性和扩展性。</p>
 * <p>分类说明：</p>
 * <ul>
 *   <li>用户模块（USER_*）— 用户认证、账户相关</li>
 *   <li>图书模块（BOOK_*）— 图书查询、操作相关</li>
 *   <li>借阅模块（BORROW_* / RECORD_* / RENEW_*）— 借还书流程相关</li>
 *   <li>权限模块（FORBIDDEN / UNAUTHORIZED）— 访问控制相关</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.common;

public final class ErrorCode {

    private ErrorCode() {}

    /** 用户不存在 */
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    /** 用户名已存在 */
    public static final String USERNAME_EXISTS = "USERNAME_EXISTS";
    /** 邮箱已被注册 */
    public static final String EMAIL_EXISTS = "EMAIL_EXISTS";
    /** 用户名或密码错误 */
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    /** 账户已被禁用 */
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    /** Token 已过期 */
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    /** Token 无效 */
    public static final String TOKEN_INVALID = "TOKEN_INVALID";

    /** 图书不存在 */
    public static final String BOOK_NOT_FOUND = "BOOK_NOT_FOUND";
    /** 图书不可借阅（非 AVAILABLE 状态） */
    public static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
    /** ISBN 重复 */
    public static final String DUPLICATE_ISBN = "DUPLICATE_ISBN";

    /** 借阅数量超出限制（上限 5 本） */
    public static final String BORROW_LIMIT_EXCEEDED = "BORROW_LIMIT_EXCEEDED";
    /** 该书已被当前用户借阅 */
    public static final String ALREADY_BORROWED = "ALREADY_BORROWED";
    /** 借阅记录不存在 */
    public static final String RECORD_NOT_FOUND = "RECORD_NOT_FOUND";
    /** 当前状态不允许此操作 */
    public static final String INVALID_STATUS = "INVALID_STATUS";
    /** 续借次数超限（仅限 1 次） */
    public static final String RENEW_LIMIT_EXCEEDED = "RENEW_LIMIT_EXCEEDED";

    /** 无操作权限 */
    public static final String FORBIDDEN = "FORBIDDEN";
    /** 未登录或 Token 缺失 */
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
}
