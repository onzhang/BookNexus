package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订阅记录视图对象 VO
 * <p>
 * 用于前端订阅列表和详情展示，聚合订阅记录 ID、图书信息、订阅状态、时间等数据。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class SubscriptionVO {

    /** 订阅记录 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 书籍 ID */
    private Long bookId;

    /** 书籍标题 */
    private String bookTitle;

    /** 书籍作者 */
    private String bookAuthor;

    /** 书籍封面 URL */
    private String bookCoverUrl;

    /** 是否活跃订阅：0=已取消，1=订阅中 */
    private Integer isActive;

    /** 订阅时间 */
    private LocalDateTime createdAt;

    /** 更新时间（取消订阅时记录） */
    private LocalDateTime updatedAt;
}
