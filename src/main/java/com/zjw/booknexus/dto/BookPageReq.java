package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 书籍分页查询请求 DTO
 * <p>
 * 用于接收前端书籍列表查询的分页参数和筛选条件，
 * 支持按关键字搜索、按状态筛选和按书架筛选。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BookPageReq {

    /** 当前页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页记录数，默认 10 条，最大 100 */
    private Integer size = 10;

    /** 关键字模糊搜索（书名、作者、ISBN） */
    private String keyword;

    /** 书籍状态筛选（AVAILABLE / BORROWED / DAMAGED / LOST） */
    private String status;

    /** 书架 ID，按指定书架筛选 */
    private Long bookshelfId;

    public Integer getPage() { return page != null && page > 0 ? page : 1; }
    public Integer getSize() { return size != null && size > 0 ? Math.min(size, 100) : 10; }
}
