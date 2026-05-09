package com.zjw.booknexus.controller;

import com.zjw.booknexus.service.SseEmitterManager;
import com.zjw.booknexus.utils.UserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 用户端通知 SSE 控制器，提供 SSE 订阅端点以支持服务端实时推送通知。
 * <p>
 * 前缀为 /api/v1/user/notifications，需登录后访问。
 * 通过 {@link SseEmitter} 建立长连接，由 {@link SseEmitterManager} 统一管理连接生命周期，
 * 当系统产生新通知时通过 SSE 通道实时推送给前端。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-08
 */
@RestController
@RequestMapping("/api/v1/user/notifications")
public class NotificationSSEController {

    @Resource
    private SseEmitterManager sseEmitterManager;

    /**
     * 订阅 SSE 推送通道接口。
     * <p>
     * GET /api/v1/user/notifications/subscribe
     * 为当前登录用户创建一个 SSE 长连接，返回 {@link SseEmitter} 实例。
     * 连接建立后，服务端可通过该通道向客户端实时推送通知消息。
     * 用户 ID 从 {@link UserContext} 中获取，无需传递请求参数。
     * </p>
     *
     * @return SseEmitter 实例，用于维持 SSE 长连接
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return sseEmitterManager.subscribe(UserContext.getUserId());
    }
}
