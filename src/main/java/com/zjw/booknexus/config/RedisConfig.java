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
 * <p>覆盖场景：</p>
 * <ul>
 *   <li>RedisTemplate — 手动操作 Redis（缓存、分布式锁计数器）</li>
 *   <li>RedisCacheManager — Spring Cache 注解支持（@Cacheable 等）</li>
 * </ul>
 * <p>序列化策略：Key 使用 StringRedisSerializer，Value 使用 Jackson2JsonRedisSerializer
 * （存储类型信息，支持泛型反序列化）。</p>
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 自定义 RedisTemplate
     * <p>设置 Key 为字符串序列化，Value 为 JSON 序列化（携带类型信息），
     * 同时注册 JavaTimeModule 以正确序列化 LocalDateTime。</p>
     *
     * @param factory Redis 连接工厂
     * @return 配置后的 RedisTemplate
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
     * Spring Cache 缓存管理器
     * <p>默认 TTL 10 分钟，禁用缓存 null 值。</p>
     *
     * @param factory Redis 连接工厂
     * @return RedisCacheManager
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
