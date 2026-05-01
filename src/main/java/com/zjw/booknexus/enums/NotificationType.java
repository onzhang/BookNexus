/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 通知类型枚举
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {

    SYSTEM("系统通知"),
    SUBSCRIPTION("订阅通知"),
    OVERDUE("逾期提醒");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
