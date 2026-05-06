package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.SubscriptionReq;
import com.zjw.booknexus.service.SubscriptionService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.SubscriptionVO;
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
 * 用户端订阅控制器，提供个人图书订阅管理功能。
 * <p>
 * 处理当前登录用户的图书归还通知订阅、取消订阅及订阅列表查询操作。
 * 当用户订阅的图书被归还后，系统会自动推送通知。
 * 所有接口均需携带有效的访问令牌进行身份认证。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 订阅图书归还通知接口。
     * <p>
     * POST /api/v1/user/subscriptions
     * 当前用户订阅指定图书的归还通知，需满足：图书存在、用户未订阅该书。
     * 当该图书被归还后，系统会自动向订阅用户发送通知。
     * </p>
     *
     * @param req 订阅请求体，包含图书 ID
     * @return 订阅记录详细信息统一响应
     */
    @PostMapping("/subscriptions")
    public Result<SubscriptionVO> subscribe(@Valid @RequestBody SubscriptionReq req) {
        return Result.created(subscriptionService.subscribe(UserContext.getUserId(), req));
    }

    /**
     * 取消订阅接口。
     * <p>
     * DELETE /api/v1/user/subscriptions/{bookId}
     * 当前用户取消对指定图书的归还通知订阅。
     * 取消后订阅记录保留但状态更新为已取消，不再接收该图书的通知。
     * </p>
     *
     * @param bookId 图书 ID
     * @return 空数据的成功响应
     */
    @DeleteMapping("/subscriptions/{bookId}")
    public Result<Void> unsubscribe(@PathVariable Long bookId) {
        subscriptionService.unsubscribe(UserContext.getUserId(), bookId);
        return Result.success();
    }

    /**
     * 查询我的订阅列表接口。
     * <p>
     * GET /api/v1/user/subscriptions
     * 分页查询当前登录用户的活跃订阅记录，结果按订阅时间倒序排列。
     * 每条记录附带图书标题、作者、封面等信息。
     * </p>
     *
     * @param page 当前页码，默认第 1 页
     * @param size 每页记录数，默认 10 条
     * @return 当前用户的订阅记录分页列表统一响应
     */
    @GetMapping("/subscriptions")
    public Result<PageResult<SubscriptionVO>> mySubscriptions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(subscriptionService.mySubscriptions(UserContext.getUserId(), page, size));
    }

    /**
     * 检查是否已订阅接口。
     * <p>
     * GET /api/v1/user/subscriptions/check/{bookId}
     * 查询当前用户是否已订阅指定图书，用于前端按钮状态展示。
     * </p>
     *
     * @param bookId 图书 ID
     * @return 是否已订阅的统一响应（true=已订阅，false=未订阅）
     */
    @GetMapping("/subscriptions/check/{bookId}")
    public Result<Boolean> checkSubscription(@PathVariable Long bookId) {
        return Result.success(subscriptionService.isSubscribed(UserContext.getUserId(), bookId));
    }
}
