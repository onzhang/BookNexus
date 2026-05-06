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
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.mapper.BookMapper;
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
 * 取 Top N 热门图书，将其基础信息预加载到 Redis Hash 缓存中，
 * 降低上班高峰期对数据库的查询压力。
 * </p>
 *
 * <p><b>Redis 数据结构：</b></p>
 * <ul>
 *   <li>热门排行：{@code booknexus:hot:books}（SortedSet）</li>
 *   <li>书籍缓存：{@code booknexus:book:{bookId}}（Hash），包含 title、author、coverUrl 等字段</li>
 *   <li>预热元信息：{@code booknexus:warmup:meta}（Hash），记录最后预热时间</li>
 * </ul>
 *
 * <p><b>预热策略：</b></p>
 * <ul>
 *   <li>取排行榜 Top 50 图书进行预热</li>
 *   <li>仅缓存基础展示字段（title、author、coverUrl），降低内存占用</li>
 *   <li>预热不设置 TTL，由业务层更新时主动失效</li>
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
    private final BookMapper bookMapper;

    /** 热门排行榜 Redis Key */
    public static final String HOT_BOOKS_KEY = "booknexus:hot:books";

    /** 预热书籍数量：排行榜 Top N */
    private static final int WARMUP_TOP_N = 50;

    /** 书籍缓存 Key 前缀 */
    private static final String BOOK_CACHE_PREFIX = "booknexus:book:";

    /** 预热元信息 Key */
    private static final String WARMUP_META_KEY = "booknexus:warmup:meta";

    /**
     * 执行缓存预热任务。
     * <p>
     * 从 Redis 热门排行榜获取 Top 50 图书 ID，查询数据库获取完整信息，
     * 将关键字段写入 Redis Hash。若排行榜为空，则跳过本次预热。
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

        // 2. 逐本查询并写入缓存
        int successCount = 0;
        int failCount = 0;

        for (String bookIdStr : topBookIds) {
            try {
                Long bookId = Long.valueOf(bookIdStr);
                Book book = bookMapper.selectById(bookId);

                if (book == null) {
                    log.warn("【缓存预热任务】图书不存在，bookId={}", bookId);
                    failCount++;
                    continue;
                }

                String cacheKey = BOOK_CACHE_PREFIX + bookId;

                // 写入 Hash 缓存：仅缓存展示所需的基础字段
                stringRedisTemplate.opsForHash().put(cacheKey, "id", String.valueOf(book.getId()));
                stringRedisTemplate.opsForHash().put(cacheKey, "title", book.getTitle() != null ? book.getTitle() : "");
                stringRedisTemplate.opsForHash().put(cacheKey, "author", book.getAuthor() != null ? book.getAuthor() : "");
                stringRedisTemplate.opsForHash().put(cacheKey, "coverUrl", book.getCoverUrl() != null ? book.getCoverUrl() : "");
                stringRedisTemplate.opsForHash().put(cacheKey, "status", book.getStatus() != null ? book.getStatus() : "");
                stringRedisTemplate.opsForHash().put(cacheKey, "availableStock", String.valueOf(book.getAvailableStock()));

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
