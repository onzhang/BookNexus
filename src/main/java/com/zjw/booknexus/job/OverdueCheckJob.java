/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * XXL-Job 逾期检查任务
 * 业务场景：每天 8:00 扫描超期未还书籍，生成逾期记录并推送催还通知
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zjw.booknexus.config.RabbitMQConfig;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.BorrowRecord;
import com.zjw.booknexus.enums.BorrowStatus;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 逾期检查定时任务
 * <p>
 * 每天 8:00 执行，扫描所有超期未还的借阅记录。
 * 对于每条逾期记录：计算逾期天数和罚金，更新借阅记录，
 * 并向 {@code notice.overdue.queue} 发送 MQ 消息触发催还通知。
 * </p>
 *
 * <p><b>业务规则：</b></p>
 * <ul>
 *   <li>逾期判定：应还日期（dueDate）&lt; 当前日期，且状态为 BORROWED 或 RENEWED</li>
 *   <li>罚金计算：0.10 元/天，按自然日计算，结果保留两位小数</li>
 *   <li>通知方式：通过 RabbitMQ 异步投递，由 NoticeOverdueConsumer 消费并生成站内通知</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueCheckJob {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.10");

    /**
     * 执行逾期检查任务。
     * <p>
     * 查询所有应还日期小于当前日期且未归还的借阅记录，
     * 逐条计算逾期天数和罚金，更新数据库，并发送催还通知到 MQ。
     * </p>
     */
    @XxlJob("overdueCheckJob")
    public void execute() {
        log.info("【逾期检查任务】开始执行，当前时间：{}", LocalDate.now());

        // 1. 查询所有超期未还记录：状态为 BORROWED 或 RENEWED，且 dueDate < 今天
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BorrowRecord::getStatus, BorrowStatus.BORROWED.name(), BorrowStatus.RENEWED.name())
                .lt(BorrowRecord::getDueDate, LocalDate.now());
        List<BorrowRecord> overdueRecords = borrowRecordMapper.selectList(wrapper);

        if (overdueRecords.isEmpty()) {
            log.info("【逾期检查任务】未发现逾期记录，任务结束");
            return;
        }

        log.info("【逾期检查任务】发现 {} 条逾期记录，开始处理", overdueRecords.size());

        // 2. 逐条处理逾期记录
        for (BorrowRecord record : overdueRecords) {
            processOverdueRecord(record);
        }

        log.info("【逾期检查任务】处理完成，共处理 {} 条逾期记录", overdueRecords.size());
    }

    /**
     * 处理单条逾期记录。
     * <p>
     * 计算逾期天数和罚金，更新借阅记录，发送 MQ 催还通知。
     * </p>
     *
     * @param record 逾期借阅记录
     */
    private void processOverdueRecord(BorrowRecord record) {
        LocalDate today = LocalDate.now();
        long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
        if (overdueDays < 0) {
            overdueDays = 0;
        }

        // 计算罚金：0.10 元/天
        BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);
        record.setFineAmount(fine);
        borrowRecordMapper.updateById(record);

        // 查询图书名称
        Book book = bookMapper.selectById(record.getBookId());
        String bookTitle = book != null ? book.getTitle() : "未知图书";

        // 组装 MQ 消息
        Map<String, Object> message = new HashMap<>();
        message.put("userId", record.getUserId());
        message.put("bookId", record.getBookId());
        message.put("overdueDays", overdueDays);
        message.put("bookTitle", bookTitle);
        message.put("fineAmount", fine);

        // 发送逾期通知到 MQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTICE_EXCHANGE,
                RabbitMQConfig.OVERDUE_ROUTING,
                message
        );

        log.info("【逾期检查任务】已发送逾期通知，userId={}，bookId={}，bookTitle={}，overdueDays={}，fine={}",
                record.getUserId(), record.getBookId(), bookTitle, overdueDays, fine);
    }
}
