/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Elasticsearch 书籍搜索 Repository
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.mapper.es;

import com.zjw.booknexus.entity.es.BookEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch 书籍搜索数据访问层
 * <p>
 * 基于 Spring Data Elasticsearch 提供书籍索引的基础 CRUD 能力。
 * 复杂查询（全文检索、拼音搜索、搜索建议）由 {@link com.zjw.booknexus.service.BookEsService} 通过
 * {@link org.springframework.data.elasticsearch.core.ElasticsearchOperations} 实现。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Repository
public interface BookEsRepository extends ElasticsearchRepository<BookEsDocument, Long> {
}
