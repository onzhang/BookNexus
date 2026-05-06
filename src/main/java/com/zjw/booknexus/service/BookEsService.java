/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * ES 书籍搜索服务接口
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.vo.BookVO;

import java.util.List;

/**
 * ES 书籍搜索服务接口
 * <p>
 * 提供基于 Elasticsearch 的书籍全文搜索、拼音搜索和搜索建议功能。
 * 当 ES 不可用或搜索失败时，自动降级到 MySQL 数据库搜索。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
public interface BookEsService {

    /**
     * 全文搜索图书。
     * <p>
     * 基于 Elasticsearch 对书名、作者、ISBN、简介进行多字段全文检索，
     * 支持中文分词（ik）和拼音搜索（pinyin）。
     * 若 ES 查询失败，自动降级为 MySQL 模糊搜索。
     * </p>
     *
     * @param keyword 搜索关键词
     * @param page    页码（从 1 开始）
     * @param size    每页大小
     * @return 图书分页结果
     */
    PageResult<BookVO> search(String keyword, int page, int size);

    /**
     * 搜索建议。
     * <p>
     * 基于 ES {@code completion suggester} 提供输入补全建议，
     * 优先从书名字段获取建议词，最多返回 10 条。
     * </p>
     *
     * @param keyword 用户输入前缀
     * @return 建议词列表
     */
    List<String> suggest(String keyword);
}
