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
 * <p>提供 Elasticsearch 高级客户端 Bean，基于 RestClient 底层传输与 JacksonJsonpMapper 序列化。
 * 注入后可在 Service 层直接调用 ES API，实现书籍全文搜索、拼音搜索（ik + pinyin 分词）、
 * 搜索建议等功能。</p>
 *
 * <p><b>涉及中间件：</b>Elasticsearch 8.x（Spring Data Elasticsearch）</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.zjw.booknexus.mapper.es")
public class ESConfig {

    /**
     * ES 服务端点地址
     * <p>从配置文件 {@code spring.elasticsearch.uris} 注入，支持多地址逗号分隔。</p>
     */
    @Value("${spring.elasticsearch.uris}")
    private String esUri;

    /**
     * 创建 Elasticsearch 高级客户端
     * <p>使用 Apache RestClient 作为底层 HTTP 传输层，JacksonJsonpMapper 负责
     * Java 对象与 JSON 文档之间的序列化与反序列化。该客户端为线程安全单例，
     * 支持书籍索引的增删改查与全文检索操作。</p>
     *
     * @return ElasticsearchClient 实例，用于操作 ES 索引与执行搜索查询
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(HttpHost.create(esUri)).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

}
