/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 借阅状态枚举
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.enums;

/**
 * 借阅状态枚举
 */
public enum BorrowStatus {

    PENDING("待审批"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    BORROWED("借阅中"),
    RENEWED("已续借"),
    RETURNED("已归还");

    private final String description;

    BorrowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
