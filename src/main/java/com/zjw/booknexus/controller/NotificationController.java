package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.NotificationPageReq;
import com.zjw.booknexus.service.NotificationService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端通知控制器，提供当前用户的通知查询和标记已读操作。
 * <p>
 * 前缀为 /api/v1/user/notifications，需登录后访问。
 * 通知由系统或 MQ 异步产生，用户只能查看和标记自己的通知。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/user/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 我的通知列表接口。
     * <p>
     * GET /api/v1/user/notifications
     * 分页查询当前登录用户的通知列表，支持按已读状态筛选，
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 通知分页结果统一响应
     */
    @GetMapping
    public Result<PageResult<NotificationVO>> page(NotificationPageReq req) {
        return Result.success(notificationService.page(req));
    }

    /**
     * 标记通知为已读接口。
     * <p>
     * PUT /api/v1/user/notifications/{id}/read
     * 将指定通知标记为已读状态，仅可操作属于自己的通知。
     * </p>
     *
     * @param id 通知 ID
     * @return 统一成功响应
     */
    /**
     * 查询未读通知数量接口。
     * <p>
     * GET /api/v1/user/notifications/unread-count
     * 返回当前用户的未读通知总数，供前端红圈角标使用。
     * </p>
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.countUnread(UserContext.getUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, UserContext.getUserId());
        return Result.success();
    }
}
