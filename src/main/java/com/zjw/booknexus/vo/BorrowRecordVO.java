package com.zjw.booknexus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录视图对象 VO
 * <p>
 * 用于前端借阅记录列表和详情展示，聚合借阅人信息、书籍信息、
 * 借阅日期、逾期罚款等数据。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BorrowRecordVO {
    /** 借阅记录 ID */
    private Long id;

    /** 借阅用户 ID */
    private Long userId;

    /** 借阅用户名 */
    private String username;

    /** 书籍 ID */
    private Long bookId;

    /** 书籍名称 */
    private String bookTitle;

    /** 借书日期 */
    private LocalDate borrowDate;

    /** 应还日期 */
    private LocalDate dueDate;

    /** 实际归还日期 */
    private LocalDate returnDate;

    /** 借阅状态（PENDING / BORROWED / RETURNED / OVERDUE） */
    private String status;

    /** 续借次数 */
    private Integer renewCount;

    /** 逾期罚款金额（元），按 0.1 元/天计算 */
    private BigDecimal fineAmount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
