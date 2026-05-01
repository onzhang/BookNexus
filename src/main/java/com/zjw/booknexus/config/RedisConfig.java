/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Redis 缓存配置：Template 序列化、Spring Cache 管理器
 * 覆盖场景：热点数据缓存、分布式锁、Session 共享
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 缓存配置
 * <p>提供 RedisTemplate 与 Spring Cache 管理器两大核心组件，
 * 覆盖缓存、分布式锁、计数器等场景。</p>
 *
 * <p><b>配置要点：</b></p>
 * <ul>
 *   <li><b>RedisTemplate</b> — 手动操作 Redis，支持 String/ Hash 结构，
 *   用于分布式锁（Redisson）、计数器、临时数据存储</li>
 *   <li><b>RedisCacheManager</b> — Spring Cache 注解支持（@Cacheable / @CachePut / @CacheEvict），
 *   默认 TTL 10 分钟，禁用缓存 null 值</li>
 * </ul>
 *
 * <p><b>序列化策略：</b></p>
 * <ul>
 *   <li>Key 使用 StringRedisSerializer（UTF-8）</li>
 *   <li>Value 使用 GenericJackson2JsonRedisSerializer（存储类型信息，支持泛型反序列化）</li>
 *   <li>注册 JavaTimeModule 以正确序列化/反序列化 LocalDateTime 等 Java 8 时间类型</li>
 * </ul>
 *
 * <p><b>涉及中间件：</b>Redis 7.x（Lettuce 客户端）</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 创建自定义 RedisTemplate
     * <p>配置 JSON 序列化策略以支持复杂对象（含泛型与 Java 8 时间类型）的存取：</p>
     * <ul>
     *   <li>Key 序列化：StringRedisSerializer.UTF_8，确保可读性</li>
     *   <li>HashKey 序列化：同 Key 策略</li>
     *   <li>Value 序列化：GenericJackson2JsonRedisSerializer，携带 {@code @class} 类型信息</li>
     *   <li>HashValue 序列化：同 Value 策略</li>
     *   <li>ObjectMapper 注册 JavaTimeModule，支持 LocalDateTime 序列化</li>
     *   <li>开启 DefaultTyping.NON_FINAL，序列化时携带非 final 类型的类信息以支持反序列化</li>
     * </ul>
     *
     * @param factory Redis 连接工厂，由 Lettuce 连接池提供
     * @return 配置完成的 RedisTemplate，线程安全可注入 Service 层使用
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);
        StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }

    /**
     * 创建 Spring Cache 缓存管理器（供 {@code @Cacheable} 等注解使用）
     * <p>配置默认缓存策略：</p>
     * <ul>
     *   <li>TTL：10 分钟（可通过 {@code @CacheConfig(cacheNames = "...")} 按需覆盖）</li>
     *   <li>Key 序列化：StringRedisSerializer.UTF_8</li>
     *   <li>Value 序列化：GenericJackson2JsonRedisSerializer（含 JavaTimeModule）</li>
     *   <li>禁用缓存 null 值，避免缓存穿透</li>
     * </ul>
     *
     * @param factory Redis 连接工厂，由 Lettuce 连接池提供
     * @return RedisCacheManager 实例，支持声明式缓存注解
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(StringRedisSerializer.UTF_8))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}
