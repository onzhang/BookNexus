package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.BookCreateReq;
import com.zjw.booknexus.dto.BookUpdateReq;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.vo.BookVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端图书管理控制器，提供图书的增删改操作。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/books。
 * 包含图书创建、信息更新、逻辑删除等功能。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    /**
     * 创建图书接口。
     * <p>
     * POST /api/v1/admin/books
     * 管理员新增图书，需提供图书基本信息（书名、作者、ISBN、分类等）,
     * 系统自动校验 ISBN 唯一性，并将图书初始状态设为 AVAILABLE。
     * </p>
     *
     * @param req 图书创建请求体，包含书名、作者、ISBN、分类 ID 列表等信息
     * @return 新创建的图书详细信息统一响应
     */
    @PostMapping
    public Result<BookVO> create(@Valid @RequestBody BookCreateReq req) {
        return Result.created(bookService.create(req));
    }

    /**
     * 更新图书信息接口。
     * <p>
     * PUT /api/v1/admin/books/{id}
     * 管理员修改指定图书的信息，支持部分字段更新（书名、作者、出版社、描述、ISBN 等）。
     * 更新 ISBN 时会校验唯一性。若提供分类 ID 列表，则同时更新图书分类关联关系。
     * </p>
     *
     * @param id  图书 ID
     * @param req 图书更新请求体，包含需要更新的字段
     * @return 更新后的图书详细信息统一响应
     */
    @PutMapping("/{id}")
    public Result<BookVO> update(@PathVariable Long id, @Valid @RequestBody BookUpdateReq req) {
        return Result.success(bookService.update(id, req));
    }

    /**
     * 删除图书接口。
     * <p>
     * DELETE /api/v1/admin/books/{id}
     * 管理员删除指定图书。逻辑删除，记录仍保留在数据库中。
     * 若图书不存在则返回 404 错误。
     * </p>
     *
     * @param id 要删除的图书 ID
     * @return 统一成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success();
    }
}
