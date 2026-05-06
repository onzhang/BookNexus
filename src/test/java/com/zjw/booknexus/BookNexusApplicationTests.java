package com.zjw.booknexus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BookNexusApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.redisson.spring.starter.RedissonAutoConfigurationV2,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration",
                "spring.redis.host=localhost",
                "spring.redis.port=6379"
        })
@ActiveProfiles("test")
class BookNexusApplicationTests {

    @Test
    void contextLoads() {
    }
}
