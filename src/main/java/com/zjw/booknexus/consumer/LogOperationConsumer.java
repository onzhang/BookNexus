package com.zjw.booknexus.consumer;

import com.zjw.booknexus.config.RabbitMQConfig;
import com.zjw.booknexus.entity.OperationLog;
import com.zjw.booknexus.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志异步写入消费者
 * <p>
 * 监听 {@code log.operation.queue}，接收由 {@link com.zjw.booknexus.aspect.OperationLogAspect}
 * 发送的操作日志消息，并将其持久化到数据库 {@code operation_log} 表。
 * 采用异步消费方式，避免操作日志写入阻塞主业务请求链路。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogOperationConsumer {

    private final OperationLogMapper operationLogMapper;

    /**
     * 消费操作日志消息并写入数据库。
     *
     * @param operationLog 操作日志实体
     */
    @RabbitListener(queues = RabbitMQConfig.LOG_QUEUE)
    public void handleLog(OperationLog operationLog) {
        try {
            if (operationLog.getCreatedAt() == null) {
                operationLog.setCreatedAt(LocalDateTime.now());
            }
            operationLogMapper.insert(operationLog);
            log.debug("操作日志已异步写入数据库，operator={}，action={}", operationLog.getOperator(), operationLog.getAction());
        } catch (Exception e) {
            log.error("操作日志写入数据库失败，operator={}，action={}，错误：{}",
                    operationLog.getOperator(), operationLog.getAction(), e.getMessage(), e);
            // 不抛出异常，避免消息无限重试阻塞队列；异常日志会被死信队列兜底
        }
    }
}
