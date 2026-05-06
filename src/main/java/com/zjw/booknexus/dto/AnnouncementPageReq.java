package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 公告分页查询请求 DTO
 * <p>
 * 用于接收前端公告列表查询的分页参数和筛选条件，
 * 支持按关键字对公告标题进行模糊搜索。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class AnnouncementPageReq {

    /** 当前页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页记录数，默认 10 条，最大 100 */
    private Integer size = 10;

    /** 关键字模糊搜索（公告标题） */
    private String keyword;

    public Integer getPage() { return page != null && page > 0 ? page : 1; }
    public Integer getSize() { return size != null && size > 0 ? Math.min(size, 100) : 10; }
}
