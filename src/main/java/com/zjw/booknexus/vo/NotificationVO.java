package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知视图对象 VO
 * <p>
 * 用于前端通知列表展示，包含通知类型、标题、内容、已读状态等信息。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class NotificationVO {

    /** 通知 ID */
    private Long id;

    /** 接收用户 ID */
    private Long userId;

    /** 通知类型：SYSTEM / SUBSCRIPTION / OVERDUE */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知正文 */
    private String content;

    /** 是否已读：0=未读，1=已读 */
    private Integer isRead;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
