package com.zjw.booknexus.service;

import com.zjw.booknexus.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器，维护用户与 SseEmitter 的映射关系，支持服务端向前端推送通知。
 * <p>
 * 使用 {@link ConcurrentHashMap} 实现线程安全的连接追踪，
 * 每个用户最多保持一个活跃连接。订阅时若用户已有连接，先完成旧连接再替换。
 * 通过 SseEmitter 的回调机制（onCompletion / onTimeout / onError）自动清理失效连接。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-08
 */
@Slf4j
@Component
public class SseEmitterManager {

    /** 用户 ID → SseEmitter 映射，线程安全 */
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /** SSE 连接超时时间：30 分钟（毫秒） */
    private static final long TIMEOUT = 30 * 60 * 1000L;

    /**
     * 订阅 SSE 推送通道。
     * <p>
     * 为指定用户创建一个 {@link SseEmitter} 实例并注册到连接池中。
     * 若该用户已存在活跃连接，先完成旧连接再建立新连接，避免连接泄漏。
     * 注册完成、超时或发生错误时自动从连接池中移除。
     * </p>
     *
     * @param userId 用户 ID
     * @return 新创建的 SseEmitter 实例
     */
    public SseEmitter subscribe(Long userId) {
        // 若用户已有连接，先完成旧连接
        SseEmitter oldEmitter = emitters.get(userId);
        if (oldEmitter != null) {
            log.info("用户 [{}] 存在旧 SSE 连接，准备完成并替换", userId);
            oldEmitter.complete();
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);
        log.info("用户 [{}] 已订阅 SSE 推送，当前连接数: {}", userId, emitters.size());

        emitter.onCompletion(() -> {
            log.info("用户 [{}] SSE 连接已完成", userId);
            remove(userId);
        });
        emitter.onTimeout(() -> {
            log.warn("用户 [{}] SSE 连接已超时", userId);
            remove(userId);
        });
        emitter.onError(ex -> {
            log.error("用户 [{}] SSE 连接发生错误: {}", userId, ex.getMessage());
            remove(userId);
        });

        return emitter;
    }

    /**
     * 向指定用户推送通知。
     * <p>
     * 通过该用户当前持有的 {@link SseEmitter} 发送 {@link NotificationVO} 数据。
     * 若发送失败（如连接已断开），静默移除该用户的连接记录，不向外抛出异常。
     * </p>
     *
     * @param userId       接收通知的用户 ID
     * @param notification 通知视图对象
     */
    public void sendNotification(Long userId, NotificationVO notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.warn("用户 [{}] 当前无活跃 SSE 连接，无法推送通知", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            log.info("已向用户 [{}] 推送通知: {}", userId, notification.getTitle());
        } catch (IOException e) {
            log.error("向用户 [{}] 推送通知失败，连接将移除: {}", userId, e.getMessage());
            remove(userId);
        }
    }

    /**
     * 移除用户的 SSE 连接记录。
     * <p>
     * 从连接池中删除指定用户的 {@link SseEmitter}，
     * 若移除后连接数发生变化则记录日志。
     * </p>
     *
     * @param userId 用户 ID
     */
    private void remove(Long userId) {
        SseEmitter removed = emitters.remove(userId);
        if (removed != null) {
            log.info("用户 [{}] SSE 连接已移除，当前连接数: {}", userId, emitters.size());
        }
    }
}
