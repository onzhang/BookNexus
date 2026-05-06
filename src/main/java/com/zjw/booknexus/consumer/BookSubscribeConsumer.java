package com.zjw.booknexus.consumer;

import com.zjw.booknexus.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 图书订阅推送消费者
 * <p>
 * 监听 {@code book.subscribe.queue}，处理用户订阅图书后的异步逻辑。
 * 当前可用于：订阅确认通知、订阅统计等扩展场景。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookSubscribeConsumer {

    /**
     * 处理订阅消息。
     *
     * @param message 消息体，包含 userId、bookId 等字段
     */
    @RabbitListener(queues = RabbitMQConfig.SUBSCRIBE_QUEUE)
    public void handleSubscribe(Map<String, Object> message) {
        log.info("收到订阅通知消息: {}", message);

        Long userId = extractLong(message, "userId");
        Long bookId = extractLong(message, "bookId");

        // TODO: 可扩展订阅确认通知、订阅统计等异步逻辑
        log.info("订阅通知处理完成，userId={}，bookId={}", userId, bookId);
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
