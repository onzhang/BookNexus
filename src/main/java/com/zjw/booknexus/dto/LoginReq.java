package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 DTO
 * <p>
 * 用于接收用户登录时提交的凭证信息，包含用户名和密码，
*  两者均为必填项。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class LoginReq {

    /** 用户名（必填） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（必填） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
