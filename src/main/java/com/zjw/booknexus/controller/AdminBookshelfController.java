package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.BookshelfCreateReq;
import com.zjw.booknexus.dto.BookshelfPageReq;
import com.zjw.booknexus.dto.BookshelfUpdateReq;
import com.zjw.booknexus.service.BookshelfService;
import com.zjw.booknexus.vo.BookshelfVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端书架管理控制器，提供书架的增删改查操作。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/bookshelves。
 * 包含书架分页查询、创建、信息更新、逻辑删除等功能。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequestMapping("/api/v1/admin/bookshelves")
@RequiredArgsConstructor
public class AdminBookshelfController {

    private final BookshelfService bookshelfService;

    /**
     * 管理员分页查询书架接口。
     * <p>
     * GET /api/v1/admin/bookshelves
     * 支持按关键词（书架名称）进行筛选，结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 书架分页结果统一响应
     */
    @GetMapping
    public Result<PageResult<BookshelfVO>> page(BookshelfPageReq req) {
        return Result.success(bookshelfService.page(req));
    }

    /**
     * 查询所有书架接口。
     * <p>
     * GET /api/v1/admin/bookshelves/all
     * 返回全部书架列表，用于下拉选择等场景。
     * </p>
     *
     * @return 书架列表统一响应
     */
    @GetMapping("/all")
    public Result<List<BookshelfVO>> listAll() {
        return Result.success(bookshelfService.listAll());
    }

    /**
     * 创建书架接口。
     * <p>
     * POST /api/v1/admin/bookshelves
     * 管理员新增书架，需提供书架名称、位置和描述信息。
     * 系统自动校验书架名称唯一性。
     * </p>
     *
     * @param req 书架创建请求体
     * @return 新创建的书架详细信息统一响应
     */
    @PostMapping
    public Result<BookshelfVO> create(@Valid @RequestBody BookshelfCreateReq req) {
        return Result.created(bookshelfService.create(req));
    }

    /**
     * 更新书架信息接口。
     * <p>
     * PUT /api/v1/admin/bookshelves/{id}
     * 管理员修改指定书架的信息，支持部分字段更新。
     * 更新名称时会校验唯一性。
     * </p>
     *
     * @param id  书架 ID
     * @param req 书架更新请求体
     * @return 更新后的书架详细信息统一响应
     */
    @PutMapping("/{id}")
    public Result<BookshelfVO> update(@PathVariable Long id, @Valid @RequestBody BookshelfUpdateReq req) {
        return Result.success(bookshelfService.update(id, req));
    }

    /**
     * 删除书架接口。
     * <p>
     * DELETE /api/v1/admin/bookshelves/{id}
     * 管理员删除指定书架。逻辑删除，记录仍保留在数据库中。
     * 若书架不存在则返回 404 错误。
     * </p>
     *
     * @param id 要删除的书架 ID
     * @return 统一成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookshelfService.delete(id);
        return Result.success();
    }
}
