/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Elasticsearch 书籍文档实体
 * 对应索引：booknexus_books
 *
 * @author 张俊文
 * @since 2026-05-06
 */
package com.zjw.booknexus.entity.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 书籍索引文档实体
 * <p>
 * 映射到 ES 索引 {@code booknexus_books}，用于全文搜索、拼音搜索和搜索建议。
 * 字段类型与 {@code sql/es_book_mapping.json} 中定义的 mapping 保持一致。
 * 设置 {@code createIndex = false}，避免 Spring Data 自动创建索引覆盖自定义 analyzer 配置。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
@Document(indexName = "booknexus_books", createIndex = false)
public class BookEsDocument {

    /** 书籍 ID */
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    /** 书名 —— ik_max_word_pinyin 分词，支持全文检索与拼音搜索 */
    @Field(type = FieldType.Text)
    private String title;

    /** 作者 —— ik_smart_pinyin 分词，支持全文检索与拼音搜索 */
    @Field(type = FieldType.Text)
    private String author;

    /** ISBN 编号 —— keyword 类型，精确匹配 */
    @Field(type = FieldType.Keyword)
    private String isbn;

    /** 出版社 */
    @Field(type = FieldType.Text)
    private String publisher;

    /** 书籍简介 —— ik 分词 */
    @Field(type = FieldType.Text)
    private String description;

    /** 关联分类名称列表 */
    @Field(type = FieldType.Keyword)
    private List<String> categoryNames;

    /** 所属书架 ID */
    @Field(type = FieldType.Long)
    private Long bookshelfId;

    /** 所属书架名称 */
    @Field(type = FieldType.Keyword)
    private String bookshelfName;

    /** 书籍状态（AVAILABLE / BORROWED / DAMAGED / LOST） */
    @Field(type = FieldType.Keyword)
    private String status;

    /** 总库存 */
    @Field(type = FieldType.Integer)
    private Integer stock;

    /** 可借库存 */
    @Field(type = FieldType.Integer)
    private Integer availableStock;

    /** 出版日期 */
    @Field(type = FieldType.Date)
    private LocalDate publishDate;

    /** 封面图片 URL —— 不建立索引 */
    @Field(type = FieldType.Keyword, index = false)
    private String coverUrl;

    /** 创建时间 */
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
}
