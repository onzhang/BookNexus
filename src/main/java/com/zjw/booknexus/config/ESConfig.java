/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Elasticsearch 全文搜索客户端配置
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 全文搜索配置
 * <p>提供 ES 高级客户端 Bean，注入后可在 Service 层直接调用 ES API
 * 实现书籍全文搜索、拼音搜索、搜索建议等功能。</p>
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.zjw.booknexus.mapper.es")
public class ESConfig {

    /** ES 服务地址，从配置文件注入 */
    @Value("${spring.elasticsearch.uris}")
    private String esUri;

    /**
     * ES 高级客户端
     * <p>使用 RestClient 底层传输，JacksonJsonpMapper 序列化。</p>
     *
     * @return ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(HttpHost.create(esUri)).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

}
