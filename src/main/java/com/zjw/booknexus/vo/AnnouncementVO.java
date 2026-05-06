package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告视图对象 VO
 * <p>
 * 用于前端公告详情和列表展示，包含公告完整信息及发布状态。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class AnnouncementVO {

    /** 公告 ID */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告正文 */
    private String content;

    /** 发布人 ID */
    private Long publisherId;

    /** 是否发布：0=草稿，1=已发布 */
    private Integer isPublished;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
