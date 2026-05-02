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
 * 由定时任务扫描逾期记录后批量推送。</p>
 * 通知类型枚举
 * <p>定义系统中通知消息的分类。不同类型通知采用不同的发送策略和展示样式：
 * SYSTEM 由管理员手动触发，SUBSCRIPTION 由订阅回调自动生成，
 * OVERDUE
 * @author 张俊文
 * @since 2026-04-30
 */
public enum NotificationType {

    /** 系统通知 — 由管理员发布，面向全体或指定用户 */
    SYSTEM("系统通知"),
    /** 订阅通知 — 用户订阅的图书上架/变更时触发的通知 */
    SUBSCRIPTION("订阅通知"),
    /** 逾期提醒 — 用户借阅图书逾期未还时自动触发的催还通知 */
    OVERDUE("逾期提醒");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
