package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.service.BookshelfService;
import com.zjw.booknexus.vo.BookshelfVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开端书架查询控制器，提供无需登录即可访问的书架列表查询。
 * <p>
 * 前缀为 /api/v1/public/bookshelves，返回全部书架列表供前端展示和筛选使用。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequestMapping("/api/v1/public/bookshelves")
@RequiredArgsConstructor
public class PublicBookshelfController {

    private final BookshelfService bookshelfService;

    /**
     * 查询所有书架接口。
     * <p>
     * GET /api/v1/public/bookshelves
     * 返回全部书架列表，无需登录即可访问。
     * </p>
     *
     * @return 书架列表统一响应
     */
    @GetMapping
    public Result<List<BookshelfVO>> listAll() {
        return Result.success(bookshelfService.listAll());
    }
}
