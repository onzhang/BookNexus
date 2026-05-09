package com.zjw.booknexus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求 DTO
 * <p>
 * 用于接收用户注册时提交的账号信息，包含用户名、密码和可选邮箱。
 * 用户名长度限制 3-50 位，密码长度限制 6-128 位。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class RegisterReq {

    /** 用户名（必填），长度 3-50 位 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    /** 密码（必填），长度 6-128 位 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度6-128位")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$", message = "密码需包含字母和数字，至少6位")
    private String password;

    /** 电子邮箱（可选），需符合邮箱格式 */
    @Email(message = "邮箱格式不正确")
    private String email;
}
