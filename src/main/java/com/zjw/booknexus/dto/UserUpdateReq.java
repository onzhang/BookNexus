package com.zjw.booknexus.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateReq {

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String status;
}
