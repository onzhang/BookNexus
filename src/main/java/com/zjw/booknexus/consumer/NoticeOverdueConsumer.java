package com.zjw.booknexus.consumer;

import com.zjw.booknexus.config.RabbitMQConfig;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.Notification;
import com.zjw.booknexus.enums.NotificationType;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 逾期催还通知消费者
 * <p>
 * 监听 {@code notice.overdue.queue}，处理图书逾期催还通知。
 * 接收到逾期消息后，向对应用户发送站内通知，提醒尽快归还图书。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeOverdueConsumer {

    private final NotificationMapper notificationMapper;
    private final BookMapper bookMapper;

    /**
     * 处理逾期通知消息。
     *
     * @param message 消息体，包含 userId、bookId、overdueDays 等字段
     */
    @RabbitListener(queues = RabbitMQConfig.OVERDUE_QUEUE)
    public void handleOverdue(Map<String, Object> message) {
        log.info("收到逾期通知消息: {}", message);
        try {
            Long userId = extractLong(message, "userId");
            Long bookId = extractLong(message, "bookId");
            Long overdueDays = extractLong(message, "overdueDays");

            if (userId == null || bookId == null) {
                log.warn("逾期消息缺少必要字段，跳过处理");
                return;
            }

            Book book = bookMapper.selectById(bookId);
            String bookTitle = book != null ? book.getTitle() : "未知图书";
            long days = overdueDays != null ? overdueDays : 0L;

            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(NotificationType.OVERDUE.name());
            notification.setTitle("图书逾期催还通知");
            notification.setContent(String.format("您借阅的图书《%s》已逾期 %d 天，请尽快归还以免产生更多罚金。", bookTitle, days));
            notification.setIsRead(0);
            notificationMapper.insert(notification);

            log.info("逾期通知已发送，userId={}，bookId={}，overdueDays={}", userId, bookId, days);
        } catch (Exception e) {
            log.error("逾期通知处理失败，消息={}，错误：{}", message, e.getMessage(), e);
        }
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
