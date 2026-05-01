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
 * <p>标识图书的流通状态，决定图书是否可被借阅。
 * 状态流转：AVAILABLE → BORROWED → RETURNED → AVAILABLE；
 * 特殊状态 DAMAGED / LOST 需管理员手动处理。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public enum BookStatus {

    /** 可借阅 — 图书在馆，可以正常借出 */
    AVAILABLE("可借阅"),
    /** 已借出 — 图书已被用户借走，不可借阅 */
    BORROWED("已借出"),
    /** 已损坏 — 图书存在物理损坏，需修复或下架处理 */
    DAMAGED("已损坏"),
    /** 已遗失 — 图书丢失，需进行遗失处理 */
    LOST("已遗失");

    private final String description;

    BookStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
