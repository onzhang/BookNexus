package com.zjw.booknexus.consumer;

import com.zjw.booknexus.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 借阅异步处理消费者
 * <p>
 * 监听 {@code book.borrow.queue}，处理借阅相关的异步逻辑。
 * 当前可用于：借阅统计、积分奖励、系统通知等扩展场景。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookBorrowConsumer {

    /**
     * 处理借阅消息。
     *
     * @param message 消息体，包含 userId、bookId 等字段
     */
    @RabbitListener(queues = RabbitMQConfig.BORROW_QUEUE)
    public void handleBorrow(Map<String, Object> message) {
        log.info("收到借阅异步消息: {}", message);

        Long userId = extractLong(message, "userId");
        Long bookId = extractLong(message, "bookId");

        // TODO: 可扩展借阅统计、积分奖励、发送系统通知等异步逻辑
        log.info("借阅异步处理完成，userId={}，bookId={}", userId, bookId);
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
