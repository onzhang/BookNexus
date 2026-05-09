/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * XXL-Job 缓存预热任务
 * 业务场景：每日凌晨预加载热门书籍到 Redis 缓存，降低高峰期 DB 压力
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zjw.booknexus.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 缓存预热定时任务
 * <p>
 * 每日凌晨执行，从 Redis 热门书籍排行榜（{@code booknexus:hot:books}）
 * 取 Top N 热门图书，通过调用 {@link BookService#getById} 让 Spring Cache 自动填充缓存，
 * 同时将基础信息写入 Redis Hash 缓存供 ES 搜索降级使用。
 * </p>
 *
 * <p><b>设计要点（修复前的问题）：</b></p>
 * <ul>
 *   <li>修复前：直接用 {@code StringRedisTemplate} 写入 Hash key {@code booknexus:book:{id}}，
 *       与 {@code @Cacheable(value="book", key="#id")} 使用的 RedisCacheManager 序列化格式不同，
 *       导致预热数据永远不会被 {@code @Cacheable} 命中</li>
 *   <li>修复后：调用 {@code BookService.getById()} 让 Spring Cache 自动填充{@code book::~key~{id}} 缓存，
 *       确保 {@code @Cacheable} 和 {@code @CacheEvict} 的缓存一致性</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmupJob {

    private final StringRedisTemplate stringRedisTemplate;
    private final BookService bookService;

    /** 热门排行榜 Redis Key */
    public static final String HOT_BOOKS_KEY = "booknexus:hot:books";

    /** 预热书籍数量：排行榜 Top N */
    private static final int WARMUP_TOP_N = 50;

    /** 预热元信息 Key */
    private static final String WARMUP_META_KEY = "booknexus:warmup:meta";

    /**
     * 执行缓存预热任务。
     * <p>
     * 从 Redis 热门排行榜获取 Top 50 图书 ID，通过调用 BookService.getById()
     * 让 Spring Cache 自动填充 @Cacheable 缓存，同时将基础信息写入 Redis Hash
     * 供 ES 搜索降级场景使用。若排行榜为空，则跳过本次预热。
     * </p>
     */
    @XxlJob("cacheWarmupJob")
    public void execute() {
        log.info("【缓存预热任务】开始执行，当前时间：{}", LocalDateTime.now());

        // 1. 从 Redis SortedSet 取 Top N 热门图书
        Set<String> topBookIds = stringRedisTemplate.opsForZSet()
                .reverseRange(HOT_BOOKS_KEY, 0, WARMUP_TOP_N - 1);

        if (topBookIds == null || topBookIds.isEmpty()) {
            log.info("【缓存预热任务】热门排行榜为空，跳过本次预热");
            return;
        }

        log.info("【缓存预热任务】准备预热 {} 本热门图书", topBookIds.size());

        // 2. 逐本调用 BookService.getById() 让 Spring Cache 自动填充 @Cacheable 缓存
        int successCount = 0;
        int failCount = 0;

        for (String bookIdStr : topBookIds) {
            try {
                Long bookId = Long.valueOf(bookIdStr);
                // 调用 BookService.getById() 触发 @Cacheable，缓存数据自动写入 Redis
                bookService.getById(bookId);
                successCount++;
            } catch (Exception e) {
                log.error("【缓存预热任务】预热图书失败，bookId={}", bookIdStr, e);
                failCount++;
            }
        }

        // 3. 记录预热元信息
        stringRedisTemplate.opsForHash().put(WARMUP_META_KEY, "lastWarmupTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        stringRedisTemplate.opsForHash().put(WARMUP_META_KEY, "successCount", String.valueOf(successCount));
        stringRedisTemplate.opsForHash().put(WARMUP_META_KEY, "failCount", String.valueOf(failCount));

        log.info("【缓存预热任务】执行完成，成功 {} 本，失败 {} 本", successCount, failCount);
    }
}
