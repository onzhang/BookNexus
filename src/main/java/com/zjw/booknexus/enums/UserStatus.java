/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 用户状态枚举
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {

    ENABLED("启用"),
    DISABLED("禁用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
