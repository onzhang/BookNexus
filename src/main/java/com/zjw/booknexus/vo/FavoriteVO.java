package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏记录视图对象 VO
 * <p>
 * 用于前端收藏列表和详情展示，聚合收藏记录 ID、图书信息、收藏时间等数据。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class FavoriteVO {

    /** 收藏记录 ID */
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

    /** 书籍状态（AVAILABLE / BORROWED / DAMAGED / LOST） */
    private String bookStatus;

    /** 收藏时间 */
    private LocalDateTime createdAt;
}
