package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 借阅记录表实体
 * <p>对应数据库表 {@code borrow_record}，存储用户的图书借阅、续借、归还全生命周期记录。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("borrow_record")
public class BorrowRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID —— 关联 {@link User} 表，非空 */
    private Long userId;
    /** 图书 ID —— 关联 {@link Book} 表，非空 */
    private Long bookId;
    /** 借阅日期 —— 非空 */
    private LocalDate borrowDate;
    /** 应还日期 —— 非空，一般为 borrowDate + 30 天 */
    private LocalDate dueDate;
    /** 实际归还日期 —— 归还后写入，逾期前为 NULL */
    private LocalDate returnDate;
    /** 状态 —— {@link com.zjw.booknexus.enums.BorrowStatus} 枚举值，PENDING / APPROVED / REJECTED / RETURNED / OVERDUE */
    private String status;
    /** 驳回原因 —— 仅当状态为 REJECTED 时有值 */
    private String rejectReason;
    /** 续借次数 —— 默认 0，最大 1（系统限制仅可续借一次） */
    private Integer renewCount;
    /** 逾期罚款金额 —— 0.1 元/天，单位元，精度 10,2 */
    private BigDecimal fineAmount;
}
