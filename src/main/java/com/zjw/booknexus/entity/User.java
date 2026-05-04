package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户表实体
 * <p>对应数据库表 {@code user}，存储系统用户（管理员和普通读者）的账号信息与个人资料。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 —— 登录凭证，唯一且非空，长度不超过 50 字符 */
    private String username;
    /** 密码 —— BCrypt 加密密文，非空，长度不超过 255 字符 */
    private String password;
    /** 邮箱 —— 用于找回密码和通知，非空，唯一 */
    private String email;
    /** 手机号 —— 可选联系方式，长度不超过 20 字符 */
    private String phone;
    /** 角色 —— {@link com.zjw.booknexus.enums.UserRole} 枚举值，ADMIN 或 USER，非空 */
    private String role;
    /** 状态 —— {@link com.zjw.booknexus.enums.UserStatus} 枚举值，ENABLED 或 DISABLED，非空 */
    private String status;
    /** 头像 URL —— MinIO 存储路径，可选，长度不超过 500 字符 */
    private String avatarUrl;
}
