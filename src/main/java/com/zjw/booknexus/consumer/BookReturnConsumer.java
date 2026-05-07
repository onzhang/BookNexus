package com.zjw.booknexus.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjw.booknexus.config.RabbitMQConfig;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.Notification;
import com.zjw.booknexus.entity.Subscription;
import com.zjw.booknexus.enums.NotificationType;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.NotificationMapper;
import com.zjw.booknexus.mapper.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 图书归还订阅通知消费者
 * <p>
 * 监听 {@code book.return.queue}，在图书归还后查询该图书的活跃订阅者，
 * 并向每位订阅者发送站内通知，提醒图书已可借阅。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookReturnConsumer {

    private final SubscriptionMapper subscriptionMapper;
    private final NotificationMapper notificationMapper;
    private final BookMapper bookMapper;

    /**
     * 处理归还消息，通知订阅者。
     *
     * @param message 消息体，包含 bookId 等字段
     */
    @RabbitListener(queues = RabbitMQConfig.RETURN_QUEUE)
    public void handleReturn(Map<String, Object> message) {
        log.info("收到图书归还消息: {}", message);
        try {
            Long bookId = extractLong(message, "bookId");
            if (bookId == null) {
                log.warn("归还消息缺少 bookId，跳过处理");
                return;
            }

            Book book = bookMapper.selectById(bookId);
            String bookTitle = book != null ? book.getTitle() : "未知图书";

            List<Subscription> subscriptions = subscriptionMapper.selectList(
                    new LambdaQueryWrapper<Subscription>()
                            .eq(Subscription::getBookId, bookId)
                            .eq(Subscription::getIsActive, 1));

            for (Subscription sub : subscriptions) {
                Notification notification = new Notification();
                notification.setUserId(sub.getUserId());
                notification.setType(NotificationType.SUBSCRIPTION.name());
                notification.setTitle("图书可借通知");
                notification.setContent("您订阅的图书《" + bookTitle + "》已归还，现在可以借阅了。");
                notification.setIsRead(0);
                notificationMapper.insert(notification);
            }

            log.info("图书归还处理完成，bookId={}，通知订阅者 {} 人", bookId, subscriptions.size());
        } catch (Exception e) {
            log.error("图书归还消息处理失败，消息={}，错误：{}", message, e.getMessage(), e);
        }
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
