package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String avatarUrl;
}
