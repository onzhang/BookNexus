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
 * <p>标识用户账户的可用性状态。ENABLED 状态可正常登录和使用系统，
 * DISABLED 状态禁止登录，通常用于账户封禁或冻结场景。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public enum UserStatus {

    /** 已启用 — 账户可正常登录和使用 */
    ENABLED("启用"),
    /** 已禁用 — 账户被锁定，禁止登录和操作 */
    DISABLED("禁用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
