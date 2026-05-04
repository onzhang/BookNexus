package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端图书查询控制器，提供图书公开查询接口。
 * <p>
 * 处理图书分页搜索和图书详情查看功能，所有接口均为公开访问，
 * 无需身份认证即可使用。支持按关键词、状态、书架等条件筛选图书。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 图书分页搜索接口。
     * <p>
     * GET /api/v1/public/books
     * 支持按关键词（书名、作者、ISBN）、图书状态、书架 ID 进行筛选，
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数，包含页码、每页大小、关键词、状态、书架 ID 等筛选条件
     * @return 分页后的图书列表统一响应
     */
    @GetMapping("/api/v1/public/books")
    public Result<PageResult<BookVO>> page(BookPageReq req) {
        return Result.success(bookService.page(req));
    }

    /**
     * 图书详情查询接口。
     * <p>
     * GET /api/v1/public/books/{id}
     * 根据图书 ID 查询图书详细信息，包含分类名称和书架名称等关联信息。
     * </p>
     *
     * @param id 图书 ID
     * @return 图书详细信息统一响应
     */
    @GetMapping("/api/v1/public/books/{id}")
    public Result<BookVO> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }
}
