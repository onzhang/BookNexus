/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * ES 书籍搜索公开接口控制器
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.service.BookEsService;
import com.zjw.booknexus.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ES 书籍搜索公开接口控制器
 * <p>
 * 提供基于 Elasticsearch 的书籍全文搜索和搜索建议接口，
 * 所有接口公开访问，无需身份认证。
 * </p>
 *
 * <p><b>接口清单：</b></p>
 * <ul>
 *   <li>GET /api/v1/public/books/search?q=keyword —— 全文搜索（标题/作者/ISBN/简介）</li>
 *   <li>GET /api/v1/public/books/suggest?q=keyword —— 搜索建议（书名补全）</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequiredArgsConstructor
public class BookSearchController {

    private final BookEsService bookEsService;

    /**
     * 书籍全文搜索接口。
     * <p>
     * GET /api/v1/public/books/search?q=keyword&amp;page=1&amp;size=20
     * 支持中文分词、拼音搜索，默认每页 20 条。
     * 若 ES 不可用，自动降级为 MySQL 模糊搜索。
     * </p>
     *
     * @param q    搜索关键词（必填）
     * @param page 页码，默认 1
     * @param size 每页大小，默认 20，最大 100
     * @return 图书分页搜索结果
     */
    @GetMapping("/api/v1/public/books/search")
    public Result<PageResult<BookVO>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        // 限制每页最大 100 条
        size = Math.min(size, 100);
        if (page < 1) {
            page = 1;
        }

        PageResult<BookVO> result = bookEsService.search(q, page, size);
        return Result.success(result);
    }

    /**
     * 搜索建议接口。
     * <p>
     * GET /api/v1/public/books/suggest?q=keyword
     * 基于 ES Completion Suggester 提供书名输入补全建议。
     * </p>
     *
     * @param q 用户输入前缀（必填）
     * @return 建议词列表
     */
    @GetMapping("/api/v1/public/books/suggest")
    public Result<List<String>> suggest(@RequestParam("q") String q) {
        List<String> suggestions = bookEsService.suggest(q);
        return Result.success(suggestions);
    }
}
