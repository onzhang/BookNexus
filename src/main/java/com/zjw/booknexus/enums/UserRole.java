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
 */
public enum UserRole {

    ADMIN("管理员"),
    USER("普通用户");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
