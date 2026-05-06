package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 书架分页查询请求 DTO
 * <p>
 * 用于管理员查询书架列表时分页参数和筛选条件，
 * 支持按关键字对书架名称进行模糊搜索。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class BookshelfPageReq {

    /** 当前页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页记录数，默认 10 条，最大 100 */
    private Integer size = 10;

    /** 关键字模糊搜索（书架名称） */
    private String keyword;

    public Integer getPage() { return page != null && page > 0 ? page : 1; }
    public Integer getSize() { return size != null && size > 0 ? Math.min(size, 100) : 10; }
}
