/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * ES 书籍搜索服务实现
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.entity.es.BookEsDocument;
import com.zjw.booknexus.service.BookEsService;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.vo.BookVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ES 书籍搜索服务实现类
 * <p>
 * 使用 Spring Data Elasticsearch 的 {@link ElasticsearchOperations} 执行复杂查询，
 * 同时通过 {@link ElasticsearchClient} 执行搜索建议（Completion Suggester）。
 * 当 ES 服务不可用或查询异常时，自动降级到 MySQL 数据库搜索，保证服务可用性。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookEsServiceImpl implements BookEsService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;
    private final BookService bookService;

    /** ES 索引名称 */
    private static final String INDEX_NAME = "booknexus_books";

    /** 搜索建议字段 */
    private static final String SUGGEST_FIELD = "title.suggest";

    /** 搜索建议返回条数 */
    private static final int SUGGEST_SIZE = 10;

    /**
     * 全文搜索图书。
     * <p>
     * 构造 {@code multi_match} 查询，对 title、author、isbn、description 加权搜索。
     * title 权重最高（3倍），author 和 isbn 次之（2倍），description 基础权重。
     * 支持 ik 中文分词和 pinyin 拼音搜索（由索引 mapping 中的 analyzer 配置自动处理）。
     * 若 ES 异常，降级调用 MySQL {@link BookService#page(BookPageReq)}。
     * </p>
     *
     * @param keyword 搜索关键词
     * @param page    页码（从 1 开始）
     * @param size    每页大小
     * @return 图书分页结果
     */
    @Override
    public PageResult<BookVO> search(String keyword, int page, int size) {
        try {
            // 1. 构建 NativeQuery：multi_match 加权查询
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("title^3", "author^2", "isbn^2", "description")
                            )
                    )
                    .withPageable(PageRequest.of(page - 1, size))
                    .build();

            // 2. 执行搜索
            SearchHits<BookEsDocument> searchHits = elasticsearchOperations.search(query, BookEsDocument.class);

            // 3. 转换为 VO 列表
            List<BookVO> records = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::convertToVO)
                    .toList();

            long total = searchHits.getTotalHits();
            return new PageResult<>(records, total, page, size);

        } catch (Exception e) {
            log.warn("【ES 搜索降级】Elasticsearch 查询异常，降级到 MySQL 搜索，keyword={}，异常：{}",
                    keyword, e.getMessage());
            return fallbackToMySQL(keyword, page, size);
        }
    }

    /**
     * 搜索建议。
     * <p>
     * 使用 Elasticsearch Completion Suggester 对 {@code title.suggest} 字段进行前缀匹配，
     * 返回最多 10 条书名建议。若 ES 异常，返回空列表。
     * </p>
     *
     * @param keyword 用户输入前缀
     * @return 建议词列表
     */
    @Override
    public List<String> suggest(String keyword) {
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .suggest(su -> su
                                    .suggesters("title-suggest", sg -> sg
                                            .prefix(keyword)
                                            .completion(c -> c
                                                    .field(SUGGEST_FIELD)
                                                    .size(SUGGEST_SIZE)
                                            )
                                    )
                            ),
                    Void.class
            );

            List<String> suggestions = new ArrayList<>();
            if (response.suggest() != null && response.suggest().containsKey("title-suggest")) {
                for (var suggest : response.suggest().get("title-suggest")) {
                    if (suggest.completion() != null) {
                        for (var option : suggest.completion().options()) {
                            suggestions.add(option.text());
                        }
                    }
                }
            }
            return suggestions;

        } catch (IOException e) {
            log.warn("【ES 建议降级】Elasticsearch 建议查询异常，keyword={}，异常：{}",
                    keyword, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * MySQL 搜索降级。
     * <p>
     * 当 ES 查询异常时，调用现有的 {@link BookService#page(BookPageReq)} 进行 MySQL 模糊搜索，
     * 保证搜索接口的可用性。
     * </p>
     *
     * @param keyword 搜索关键词
     * @param page    页码
     * @param size    每页大小
     * @return MySQL 分页结果
     */
    private PageResult<BookVO> fallbackToMySQL(String keyword, int page, int size) {
        BookPageReq req = new BookPageReq();
        req.setKeyword(keyword);
        req.setPage(page);
        req.setSize(size);
        return bookService.page(req);
    }

    /**
     * 将 ES 文档转换为视图对象。
     *
     * @param doc ES 书籍文档
     * @return 图书视图对象
     */
    private BookVO convertToVO(BookEsDocument doc) {
        BookVO vo = new BookVO();
        vo.setId(doc.getId());
        vo.setTitle(doc.getTitle());
        vo.setAuthor(doc.getAuthor());
        vo.setIsbn(doc.getIsbn());
        vo.setPublisher(doc.getPublisher());
        vo.setDescription(doc.getDescription());
        vo.setCoverUrl(doc.getCoverUrl());
        vo.setStock(doc.getStock());
        vo.setAvailableStock(doc.getAvailableStock());
        vo.setStatus(doc.getStatus());
        vo.setBookshelfId(doc.getBookshelfId());
        vo.setBookshelfName(doc.getBookshelfName());
        vo.setCategoryNames(doc.getCategoryNames());
        vo.setPublishDate(doc.getPublishDate());
        vo.setCreatedAt(doc.getCreatedAt());
        vo.setUpdatedAt(doc.getUpdatedAt());
        return vo;
    }
}
