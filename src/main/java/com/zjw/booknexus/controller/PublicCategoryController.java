package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.service.CategoryService;
import com.zjw.booknexus.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开端分类查询控制器，提供无需登录即可访问的分类列表和树形查询。
 * <p>
 * 前缀为 /api/v1/public/categories，返回分类列表和树形结构供前端展示和筛选使用。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequestMapping("/api/v1/public/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    /**
     * 查询所有分类接口。
     * <p>
     * GET /api/v1/public/categories
     * 返回全部分类列表，无需登录即可访问。
     * </p>
     *
     * @return 分类列表统一响应
     */
    @GetMapping
    public Result<List<CategoryVO>> listAll() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 查询分类树形结构接口。
     * <p>
     * GET /api/v1/public/categories/tree
     * 返回按 parentId 组装好的树形结构分类数据，便于前端展示层级关系。
     * </p>
     *
     * @return 分类树列表统一响应
     */
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }
}
