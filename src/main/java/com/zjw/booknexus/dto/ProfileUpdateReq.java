package com.zjw.booknexus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户个人资料更新请求 DTO
 * <p>
 * 用于当前登录用户更新自己的个人资料信息，
 * 包含用户名、邮箱和手机号。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-07
 */
@Data
public class ProfileUpdateReq {

    /** 用户名（必填） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 电子邮箱（必填），需符合邮箱格式 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号码（可选），需符合中国大陆手机号格式 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
