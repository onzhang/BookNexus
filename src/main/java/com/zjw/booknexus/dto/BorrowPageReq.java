package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 用户借阅记录分页查询请求 DTO
 * <p>
 * 用于接收当前登录用户查询个人借阅记录的分页参数和筛选条件，
 * 支持按借阅状态进行筛选。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BorrowPageReq {

    /** 当前页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页记录数，默认 10 条，最大 100 */
    private Integer size = 10;

    /** 借阅状态筛选（PENDING / BORROWED / RETURNED / OVERDUE） */
    private String status;

    public Integer getPage() { return page != null && page > 0 ? page : 1; }
    public Integer getSize() { return size != null && size > 0 ? Math.min(size, 100) : 10; }
}
