/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 统一分页响应封装
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.common;

import java.util.List;

/**
 * 统一分页响应封装
 * <p>与 MyBatis-Plus {@link com.baomidou.mybatisplus.core.metadata.IPage IPage}
 * 配合使用，所有分页查询接口统一返回此结构，保证前端分页组件数据格式一致。</p>
 *
 * <p>字段计算规则：{@code pages = (total + size - 1) / size}，当 {@code size <= 0} 时 {@code pages = 0}。</p>
 *
 * @param <T> 列表数据类型
 * @author 张俊文
 * @since 2026-04-30
 */
public class PageResult<T> {

    /** 当前页数据列表 */
    private List<T> records;

    /** 总记录数（满足查询条件的全部记录数量） */
    private long total;

    /** 当前页码（从 1 开始计数） */
    private long page;

    /** 每页记录数 */
    private long size;

    /** 总页数（根据 total 和 size 自动计算） */
    private long pages;

    /** 默认构造器 */
    public PageResult() {}

    /**
     * 构造分页结果
     *
     * @param records 当前页数据
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页大小
     */
    public PageResult(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (size > 0) ? (total + size - 1) / size : 0;
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public long getPages() { return pages; }
    public void setPages(long pages) { this.pages = pages; }

}
