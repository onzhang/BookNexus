/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * RabbitMQ 消息队列配置：交换机、队列、路由绑定
 * 业务场景：借阅异步处理、归还订阅通知、操作日志写入、逾期提醒
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置
 * <p>业务场景：</p>
 * <ul>
 *   <li>book.borrow — 借阅申请异步处理（削峰）</li>
 *   <li>book.return — 归还事件，触发订阅通知</li>
 *   <li>book.subscribe — 图书订阅推送</li>
 *   <li>log.operation — 操作日志异步写入</li>
 *   <li>notice.overdue — 逾期催还通知</li>
 * </ul>
 * <p>所有交换机类型为 Topic，支持路由键模糊匹配，便于后续扩展。</p>
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 交换机名称 ====================

    /** 图书业务交换机（借阅、归还、订阅） */
    public static final String BOOK_EXCHANGE = "book.exchange";

    /** 日志业务交换机 */
    public static final String LOG_EXCHANGE = "log.exchange";

    /** 通知业务交换机（逾期、催还） */
    public static final String NOTICE_EXCHANGE = "notice.exchange";

    // ==================== 队列名称 ====================

    /** 借阅处理队列 */
    public static final String BORROW_QUEUE = "book.borrow.queue";

    /** 归还通知队列 */
    public static final String RETURN_QUEUE = "book.return.queue";

    /** 订阅推送队列 */
    public static final String SUBSCRIBE_QUEUE = "book.subscribe.queue";

    /** 操作日志写入队列 */
    public static final String LOG_QUEUE = "log.operation.queue";

    /** 逾期提醒队列 */
    public static final String OVERDUE_QUEUE = "notice.overdue.queue";

    // ==================== 路由键 ====================

    /** 借阅路由键 */
    public static final String BORROW_ROUTING = "book.borrow";

    /** 归还路由键 */
    public static final String RETURN_ROUTING = "book.return";

    /** 订阅路由键 */
    public static final String SUBSCRIBE_ROUTING = "book.subscribe";

    /** 日志路由键 */
    public static final String LOG_ROUTING = "log.operation";

    /** 逾期路由键 */
    public static final String OVERDUE_ROUTING = "notice.overdue";

    // ==================== 死信队列 ====================

    /** 死信交换机名称 */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    /** 死信队列名称 */
    public static final String DLX_QUEUE = "dlx.queue";

    /** 死信路由键 */
    public static final String DLX_ROUTING = "dlx.routing";

    /**
     * 消息转换器
     * <p>所有消息自动序列化为 JSON 格式，消费者端也使用此转换器反序列化。</p>
     *
     * @return Jackson2JsonMessageConverter
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 发送消息模板
     *
     * @param connectionFactory 连接工厂
     * @param converter         消息转换器
     * @return RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                        Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    /**
     * 图书业务 Topic 交换机
     *
     * @return TopicExchange（持久化、非自动删除）
     */
    @Bean
    public TopicExchange bookExchange() {
        return new TopicExchange(BOOK_EXCHANGE, true, false);
    }

    /**
     * 日志业务 Topic 交换机
     *
     * @return TopicExchange
     */
    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange(LOG_EXCHANGE, true, false);
    }

    /**
     * 通知业务 Topic 交换机
     *
     * @return TopicExchange
     */
    @Bean
    public TopicExchange noticeExchange() {
        return new TopicExchange(NOTICE_EXCHANGE, true, false);
    }

    /** 借阅处理队列（持久化、非独占、不自动删除，死信投递到 dlx.exchange） */
    @Bean
    public Queue borrowQueue() {
        return QueueBuilder.durable(BORROW_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /** 归还通知队列（死信投递到 dlx.exchange） */
    @Bean
    public Queue returnQueue() {
        return QueueBuilder.durable(RETURN_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /** 订阅推送队列（死信投递到 dlx.exchange） */
    @Bean
    public Queue subscribeQueue() {
        return QueueBuilder.durable(SUBSCRIBE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /** 操作日志写入队列（死信投递到 dlx.exchange） */
    @Bean
    public Queue logQueue() {
        return QueueBuilder.durable(LOG_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /** 逾期提醒队列（死信投递到 dlx.exchange） */
    @Bean
    public Queue overdueQueue() {
        return QueueBuilder.durable(OVERDUE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /** 绑定借阅队列到图书交换机 */
    @Bean
    public Binding borrowBinding() { return BindingBuilder.bind(borrowQueue()).to(bookExchange()).with(BORROW_ROUTING); }

    /** 绑定归还队列到图书交换机 */
    @Bean
    public Binding returnBinding() { return BindingBuilder.bind(returnQueue()).to(bookExchange()).with(RETURN_ROUTING); }

    /** 绑定订阅队列到图书交换机 */
    @Bean
    public Binding subscribeBinding() { return BindingBuilder.bind(subscribeQueue()).to(bookExchange()).with(SUBSCRIBE_ROUTING); }

    /** 绑定日志队列到日志交换机 */
    @Bean
    public Binding logBinding() { return BindingBuilder.bind(logQueue()).to(logExchange()).with(LOG_ROUTING); }

    /** 绑定逾期队列到通知交换机 */
    @Bean
    public Binding overdueBinding() { return BindingBuilder.bind(overdueQueue()).to(noticeExchange()).with(OVERDUE_ROUTING); }

    // ==================== 死信队列 ====================

    /** 死信 Topic 交换机（持久化、非自动删除） */
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    /** 死信队列（持久化） */
    @Bean
    public Queue dlxQueue() { return QueueBuilder.durable(DLX_QUEUE).build(); }

    /** 绑定死信队列到死信交换机（接收所有死信消息） */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with("#");
    }

}
