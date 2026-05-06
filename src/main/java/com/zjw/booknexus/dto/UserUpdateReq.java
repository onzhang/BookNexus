package com.zjw.booknexus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户信息更新请求 DTO
 * <p>
 * 用于管理员更新用户信息时提交的修改数据，所有字段均为可选，
 * 仅对非空字段进行更新操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class UserUpdateReq {

    /** 电子邮箱（可选），需符合邮箱格式 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号码 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 用户状态（ENABLED / DISABLED） */
    private String status;
}
