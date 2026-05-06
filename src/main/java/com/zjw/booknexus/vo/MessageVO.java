package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言视图对象 VO
 * <p>
 * 用于前端留言列表展示，包含用户留言内容、管理员回复及回复时间等信息。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class MessageVO {

    /** 留言 ID */
    private Long id;

    /** 留言用户 ID */
    private Long userId;

    /** 留言内容 */
    private String content;

    /** 管理员回复 */
    private String reply;

    /** 回复时间 */
    private LocalDateTime replyAt;

    /** 回复人 ID */
    private Long replierId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
