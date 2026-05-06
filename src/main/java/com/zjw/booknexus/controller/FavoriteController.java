package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.FavoriteReq;
import com.zjw.booknexus.service.FavoriteService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.FavoriteVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端收藏控制器，提供个人收藏管理功能。
 * <p>
 * 处理当前登录用户的图书收藏、取消收藏及收藏列表查询操作。
 * 所有接口均需携带有效的访问令牌进行身份认证。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏图书接口。
     * <p>
     * POST /api/v1/user/favorites
     * 当前用户收藏指定图书，需满足：图书存在、用户未收藏该书。
     * 收藏成功后可在"我的收藏"列表中查看。
     * </p>
     *
     * @param req 收藏请求体，包含图书 ID
     * @return 收藏记录详细信息统一响应
     */
    @PostMapping("/favorites")
    public Result<FavoriteVO> addFavorite(@Valid @RequestBody FavoriteReq req) {
        return Result.created(favoriteService.addFavorite(UserContext.getUserId(), req));
    }

    /**
     * 取消收藏接口。
     * <p>
     * DELETE /api/v1/user/favorites/{bookId}
     * 当前用户取消对指定图书的收藏。仅删除当前用户自己的收藏记录。
     * </p>
     *
     * @param bookId 图书 ID
     * @return 空数据的成功响应
     */
    @DeleteMapping("/favorites/{bookId}")
    public Result<Void> removeFavorite(@PathVariable Long bookId) {
        favoriteService.removeFavorite(UserContext.getUserId(), bookId);
        return Result.success();
    }

    /**
     * 查询我的收藏列表接口。
     * <p>
     * GET /api/v1/user/favorites
     * 分页查询当前登录用户的收藏记录，结果按收藏时间倒序排列。
     * 每条记录附带图书标题、作者、封面等信息。
     * </p>
     *
     * @param page 当前页码，默认第 1 页
     * @param size 每页记录数，默认 10 条
     * @return 当前用户的收藏记录分页列表统一响应
     */
    @GetMapping("/favorites")
    public Result<PageResult<FavoriteVO>> myFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(favoriteService.myFavorites(UserContext.getUserId(), page, size));
    }

    /**
     * 检查是否已收藏接口。
     * <p>
     * GET /api/v1/user/favorites/check/{bookId}
     * 查询当前用户是否已收藏指定图书，用于前端按钮状态展示。
     * </p>
     *
     * @param bookId 图书 ID
     * @return 是否已收藏的统一响应（true=已收藏，false=未收藏）
     */
    @GetMapping("/favorites/check/{bookId}")
    public Result<Boolean> checkFavorite(@PathVariable Long bookId) {
        return Result.success(favoriteService.isFavorited(UserContext.getUserId(), bookId));
    }
}
