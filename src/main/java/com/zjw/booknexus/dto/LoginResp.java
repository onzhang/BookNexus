package com.zjw.booknexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户登录响应 DTO
 * <p>
 * 登录成功后返回的令牌及用户基本信息，
 * 包含访问令牌（30 分钟有效）和刷新令牌（7 天有效）。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@AllArgsConstructor
public class LoginResp {
    /** 访问令牌（Access Token），有效期 30 分钟 */
    private String accessToken;

    /** 刷新令牌（Refresh Token），有效期 7 天 */
    private String refreshToken;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户角色（ADMIN / USER） */
    private String role;
}
