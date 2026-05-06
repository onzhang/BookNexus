/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * XXL-Job 热门排行同步任务
 * 业务场景：每小时从借阅记录统计热门书籍，更新到 Redis SortedSet
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zjw.booknexus.entity.BorrowRecord;
import com.zjw.booknexus.enums.BorrowStatus;
import com.zjw.booknexus.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 热门排行同步定时任务
 * <p>
 * 每小时执行一次，从借阅记录统计最近一段时间（默认 30 天）内
 * 被借阅次数最多的图书，将排行榜数据写入 Redis SortedSet，
 * 供前端热门推荐和缓存预热任务使用。
 * </p>
 *
 * <p><b>Redis 数据结构：</b></p>
 * <ul>
 *   <li>Key: {@code booknexus:hot:books}</li>
 *   <li>Type: SortedSet（ZSET），score 为借阅次数，member 为 bookId</li>
 *   <li>TTL: 无过期，由本任务每小时全量覆盖更新</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotBookSyncJob {

    private final BorrowRecordMapper borrowRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /** Redis 热门书籍排行榜 Key */
    public static final String HOT_BOOKS_KEY = "booknexus:hot:books";

    /** 统计时间窗口：最近 30 天 */
    private static final int STAT_DAYS = 30;

    /**
     * 执行热门排行同步任务。
     * <p>
     * 统计最近 30 天内所有成功的借阅记录（状态为 BORROWED、RENEWED 或 RETURNED），
     * 按图书 ID 分组计数，将结果写入 Redis SortedSet。
     * 写入前会先清空旧数据，确保排行榜为全量最新结果。
     * </p>
     */
    @XxlJob("hotBookSyncJob")
    public void execute() {
        log.info("【热门排行同步任务】开始执行，当前时间：{}", LocalDateTime.now());

        // 1. 查询最近 30 天内的借阅记录（排除 PENDING 和 REJECTED 状态）
        LocalDateTime startTime = LocalDateTime.now().minusDays(STAT_DAYS);
        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getCreatedAt, startTime)
                        .in(BorrowRecord::getStatus,
                                BorrowStatus.BORROWED.name(),
                                BorrowStatus.RENEWED.name(),
                                BorrowStatus.RETURNED.name())
        );

        if (records.isEmpty()) {
            log.info("【热门排行同步任务】最近 {} 天内无借阅记录，清空排行榜", STAT_DAYS);
            stringRedisTemplate.delete(HOT_BOOKS_KEY);
            return;
        }

        // 2. 按图书 ID 分组统计借阅次数
        Map<Long, Long> bookBorrowCount = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        log.info("【热门排行同步任务】统计到 {} 种图书，共 {} 次借阅",
                bookBorrowCount.size(), records.size());

        // 3. 清空旧排行榜并写入新数据
        stringRedisTemplate.delete(HOT_BOOKS_KEY);

        for (Map.Entry<Long, Long> entry : bookBorrowCount.entrySet()) {
            stringRedisTemplate.opsForZSet().add(
                    HOT_BOOKS_KEY,
                    String.valueOf(entry.getKey()),
                    entry.getValue().doubleValue()
            );
        }

        // 4. 记录执行时间戳
        stringRedisTemplate.opsForHash().put(
                "booknexus:hot:meta",
                "lastSyncTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        log.info("【热门排行同步任务】排行榜更新完成，共 {} 本图书", bookBorrowCount.size());
    }
}
