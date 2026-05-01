package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 令牌刷新请求 DTO
 * <p>
 * 用于接收客户端提交的刷新令牌，以获取新的访问令牌和刷新令牌。
 * 刷新令牌有效期为 7 天，过期后需重新登录。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class RefreshReq {

    /** 刷新令牌（必填），用于获取新的访问令牌 */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
