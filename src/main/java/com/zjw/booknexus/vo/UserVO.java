package com.zjw.booknexus.vo;

import lombok.Data;

/**
 * 用户视图对象 VO
 * <p>
 * 用于前端用户信息展示，脱敏后返回用户基本信息，
 * 不包含密码等敏感字段。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class UserVO {
    /** 用户 ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 电子邮箱 */
    private String email;

    /** 手机号码 */
    private String phone;

    /** 用户角色（ADMIN / USER） */
    private String role;

    /** 用户状态（ENABLED / DISABLED） */
    private String status;

    /** 头像 URL */
    private String avatarUrl;

    /** 注册时间 */
    private String createdAt;
}
