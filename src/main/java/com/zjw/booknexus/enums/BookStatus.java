/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 图书状态枚举
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.enums;

/**
 * 图书状态枚举
 */
public enum BookStatus {

    AVAILABLE("可借阅"),
    BORROWED("已借出"),
    DAMAGED("已损坏"),
    LOST("已遗失");

    private final String description;

    BookStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
