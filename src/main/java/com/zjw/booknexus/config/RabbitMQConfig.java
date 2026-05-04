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
 * <p>定义系统中所有消息队列的交换机、队列、路由键及绑定关系，
 * 并配置死信队列（DLX）兜底未处理的消息。</p>
 *
 * <p><b>业务场景：</b></p>
 * <ul>
 *   <li>book.borrow — 借阅申请异步处理（削峰填谷）</li>
 *   <li>book.return — 归还事件，触发订阅通知</li>
 *   <li>book.subscribe — 图书订阅推送</li>
 *   <li>log.operation — 操作日志异步写入</li>
 *   <li>notice.overdue — 逾期催还通知</li>
 * </ul>
 *
 * <p><b>架构设计：</b></p>
 * <ul>
 *   <li>3 个 Topic 交换机（book / log / notice），支持路由键模糊匹配便于扩展</li>
 *   <li>5 个业务队列，均绑定死信交换机 dlx.exchange（重试耗尽后自动转入）</li>
 *   <li>1 个死信交换机 + 1 个死信队列，兜底处理所有无法正常消费的消息</li>
 *   <li>消息体使用 Jackson2JsonMessageConverter 统一 JSON 序列化</li>
 * </ul>
 *
 * <p><b>涉及中间件：</b>RabbitMQ（AMQP 协议）</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 交换机名称 ====================

    /** 图书业务 Topic 交换机（处理借阅、归还、订阅相关消息） */
    public static final String BOOK_EXCHANGE = "book.exchange";

    /** 日志业务 Topic 交换机（处理操作日志异步写入消息） */
    public static final String LOG_EXCHANGE = "log.exchange";

    /** 通知业务 Topic 交换机（处理逾期催还、系统通知等消息） */
    public static final String NOTICE_EXCHANGE = "notice.exchange";

    // ==================== 队列名称 ====================

    /** 借阅处理队列，消费者异步处理借阅申请逻辑（检查库存、更新状态） */
    public static final String BORROW_QUEUE = "book.borrow.queue";

    /** 归还通知队列，消费者处理归还事件后的订阅通知推送 */
    public static final String RETURN_QUEUE = "book.return.queue";

    /** 订阅推送队列，消费者处理图书可借提醒推送 */
    public static final String SUBSCRIBE_QUEUE = "book.subscribe.queue";

    /** 操作日志写入队列，消费者异步将操作日志持久化到数据库 */
    public static final String LOG_QUEUE = "log.operation.queue";

    /** 逾期提醒队列，消费者处理逾期图书的催还通知发送 */
    public static final String OVERDUE_QUEUE = "notice.overdue.queue";

    // ==================== 路由键 ====================

    /** 借阅消息路由键，匹配 book.borrow.* 事件 */
    public static final String BORROW_ROUTING = "book.borrow";

    /** 归还消息路由键，匹配 book.return.* 事件 */
    public static final String RETURN_ROUTING = "book.return";

    /** 订阅消息路由键，匹配 book.subscribe.* 事件 */
    public static final String SUBSCRIBE_ROUTING = "book.subscribe";

    /** 日志消息路由键，匹配 log.operation.* 事件 */
    public static final String LOG_ROUTING = "log.operation";

    /** 逾期消息路由键，匹配 notice.overdue.* 事件 */
    public static final String OVERDUE_ROUTING = "notice.overdue";

    // ==================== 死信队列 ====================

    /** 死信交换机名称，所有业务队列的死信统一投递至此交换机 */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    /** 死信队列名称，兜底存储所有重试耗尽后转入死信的异常消息 */
    public static final String DLX_QUEUE = "dlx.queue";

    /** 死信路由键，死信消息投递到 dlx.queue 时使用的路由键 */
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

    /**
     * 创建借阅处理队列
     * <p>持久化队列，消费者异步处理借阅申请。配置死信交换机 dlx.exchange，
     * 当消费者拒绝、NACK 或队列 TTL 到期时，消息自动转发至死信队列兜底。</p>
     *
     * @return 持久化的 Queue 实例
     */
    @Bean
    public Queue borrowQueue() {
        return QueueBuilder.durable(BORROW_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /**
     * 创建归还通知队列
     * <p>持久化队列，处理图书归还后的订阅通知推送。配置死信投递策略。</p>
     *
     * @return 持久化的 Queue 实例
     */
    @Bean
    public Queue returnQueue() {
        return QueueBuilder.durable(RETURN_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /**
     * 创建订阅推送队列
     * <p>持久化队列，处理用户订阅图书可借时的消息推送。配置死信投递策略。</p>
     *
     * @return 持久化的 Queue 实例
     */
    @Bean
    public Queue subscribeQueue() {
        return QueueBuilder.durable(SUBSCRIBE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /**
     * 创建操作日志写入队列
     * <p>持久化队列，消费者异步将管理端与用户操作日志写入数据库，
     * 降低请求链路的写入延迟。配置死信投递策略。</p>
     *
     * @return 持久化的 Queue 实例
     */
    @Bean
    public Queue logQueue() {
        return QueueBuilder.durable(LOG_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /**
     * 创建逾期提醒队列
     * <p>持久化队列，处理借阅逾期图书的催还通知发送，
     * 支持短信/站内信等通知方式。配置死信投递策略。</p>
     *
     * @return 持久化的 Queue 实例
     */
    @Bean
    public Queue overdueQueue() {
        return QueueBuilder.durable(OVERDUE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING)
                .build();
    }

    /**
     * 绑定借阅队列到图书交换机
     * <p>路由键：{@code book.borrow}，匹配借阅事件消息。</p>
     *
     * @return Binding 绑定关系
     */
    @Bean
    public Binding borrowBinding() { return BindingBuilder.bind(borrowQueue()).to(bookExchange()).with(BORROW_ROUTING); }

    /**
     * 绑定归还队列到图书交换机
     * <p>路由键：{@code book.return}，匹配归还事件消息。</p>
     *
     * @return Binding 绑定关系
     */
    @Bean
    public Binding returnBinding() { return BindingBuilder.bind(returnQueue()).to(bookExchange()).with(RETURN_ROUTING); }

    /**
     * 绑定订阅队列到图书交换机
     * <p>路由键：{@code book.subscribe}，匹配订阅推送消息。</p>
     *
     * @return Binding 绑定关系
     */
    @Bean
    public Binding subscribeBinding() { return BindingBuilder.bind(subscribeQueue()).to(bookExchange()).with(SUBSCRIBE_ROUTING); }

    /**
     * 绑定日志队列到日志交换机
     * <p>路由键：{@code log.operation}，匹配操作日志消息。</p>
     *
     * @return Binding 绑定关系
     */
    @Bean
    public Binding logBinding() { return BindingBuilder.bind(logQueue()).to(logExchange()).with(LOG_ROUTING); }

    /**
     * 绑定逾期队列到通知交换机
     * <p>路由键：{@code notice.overdue}，匹配逾期催还消息。</p>
     *
     * @return Binding 绑定关系
     */
    @Bean
    public Binding overdueBinding() { return BindingBuilder.bind(overdueQueue()).to(noticeExchange()).with(OVERDUE_ROUTING); }

    // ==================== 死信队列 ====================

    /**
     * 创建死信 Topic 交换机
     * <p>持久化、非自动删除。所有业务队列重试耗尽后的死信消息
     * 统一投递至此交换机进行兜底处理。</p>
     *
     * @return TopicExchange 死信交换机
     */
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    /**
     * 创建死信队列
     * <p>持久化队列，接收所有死信交换机路由的消息，
     * 用于人工介入排查消费失败原因或重新投递。</p>
     *
     * @return Queue 死信队列
     */
    @Bean
    public Queue dlxQueue() { return QueueBuilder.durable(DLX_QUEUE).build(); }

    /**
     * 绑定死信队列到死信交换机
     * <p>使用通配路由键 {@code #} 匹配所有路由键，
     * 确保所有来源的死信消息都能被死信队列接收。</p>
     *
     * @return Binding 绑定关系
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with("#");
    }

}
