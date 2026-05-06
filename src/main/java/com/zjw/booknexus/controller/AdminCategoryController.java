package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.CategoryCreateReq;
import com.zjw.booknexus.dto.CategoryPageReq;
import com.zjw.booknexus.dto.CategoryUpdateReq;
import com.zjw.booknexus.service.CategoryService;
import com.zjw.booknexus.vo.CategoryVO;
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
 * 管理端分类管理控制器，提供分类的增删改查操作。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/categories。
 * 包含分类分页查询、树形查询、创建、信息更新、逻辑删除等功能。
 * 支持多级树形结构（通过 parentId 自关联）。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 管理员分页查询分类接口。
     * <p>
     * GET /api/v1/admin/categories
     * 支持按关键词（分类名称）进行筛选，结果按排序序号和创建时间排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 分类分页结果统一响应
     */
    @GetMapping
    public Result<PageResult<CategoryVO>> page(CategoryPageReq req) {
        return Result.success(categoryService.page(req));
    }

    /**
     * 查询所有分类接口。
     * <p>
     * GET /api/v1/admin/categories/all
     * 返回全部分类列表，用于下拉选择等场景。
     * </p>
     *
     * @return 分类列表统一响应
     */
    @GetMapping("/all")
    public Result<List<CategoryVO>> listAll() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 查询分类树形结构接口。
     * <p>
     * GET /api/v1/admin/categories/tree
     * 返回按 parentId 组装好的树形结构分类数据，便于前端展示层级关系。
     * </p>
     *
     * @return 分类树列表统一响应
     */
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    /**
     * 创建分类接口。
     * <p>
     * POST /api/v1/admin/categories
     * 管理员新增分类，需提供分类名称、父分类 ID 和排序序号。
     * 系统自动校验分类名称唯一性。parentId 为 0 表示顶级分类。
     * </p>
     *
     * @param req 分类创建请求体
     * @return 新创建的分类详细信息统一响应
     */
    @PostMapping
    public Result<CategoryVO> create(@Valid @RequestBody CategoryCreateReq req) {
        return Result.created(categoryService.create(req));
    }

    /**
     * 更新分类信息接口。
     * <p>
     * PUT /api/v1/admin/categories/{id}
     * 管理员修改指定分类的信息，支持部分字段更新。
     * 更新名称时会校验唯一性。不允许将父分类设为其自身。
     * </p>
     *
     * @param id  分类 ID
     * @param req 分类更新请求体
     * @return 更新后的分类详细信息统一响应
     */
    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateReq req) {
        return Result.success(categoryService.update(id, req));
    }

    /**
     * 删除分类接口。
     * <p>
     * DELETE /api/v1/admin/categories/{id}
     * 管理员删除指定分类。逻辑删除，记录仍保留在数据库中。
     * 若该分类下存在子分类，不允许删除。
     * 若分类不存在则返回 404 错误。
     * </p>
     *
     * @param id 要删除的分类 ID
     * @return 统一成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
