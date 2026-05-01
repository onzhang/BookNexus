/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 用户角色枚举
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.enums;

/**
 * 用户角色枚举
 * <p>定义系统内用户角色类型。系统采用简化角色模型（非完整 RBAC），
 * 通过角色枚举判断操作权限：ADMIN 可访问管理端接口，USER 仅可访问用户端接口。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public enum UserRole {

    /** 系统管理员 — 拥有所有管理端操作权限 */
    ADMIN("管理员"),
    /** 普通用户 — 仅拥有用户端基本操作权限 */
    USER("普通用户");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
