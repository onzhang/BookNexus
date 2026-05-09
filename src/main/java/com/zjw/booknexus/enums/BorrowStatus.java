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
 * <p>标识借阅记录的完整生命周期状态。
 * 状态机流转路径：</p>
 * <pre>
 * PENDING ──┬── APPROVED ──┬── BORROWED ──┬── RENEWED ──┬── RETURNED
 *           │              │               │              │
 *           └── REJECTED   └───────────────┘              └── (逾期 → 缴纳罚款后归还)
 * </pre>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public enum BorrowStatus {

    /** 待审批 — 用户已提交借阅申请，等待管理员审批 */
    PENDING("待审批"),
    /** 已批准 — 管理员已通过借阅审批 */
    APPROVED("已批准"),
    /** 已拒绝 — 管理员已驳回借阅申请 */
    REJECTED("已拒绝"),
    /** 借阅中 — 图书已出库，用户持有中 */
    BORROWED("借阅中"),
    /** 已续借 — 用户已申请续借并成功延长归还期限 */
    RENEWED("已续借"),
    /** 待归还确认 — 用户已归还图书，等待管理员确认入库 */
    RETURN_PENDING("待归还确认"),
    /** 已归还 — 图书已归还入库，借阅流程结束 */
    RETURNED("已归还");

    private final String description;

    BorrowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
