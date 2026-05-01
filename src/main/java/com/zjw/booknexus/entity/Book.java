package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 图书表实体
 * <p>对应数据库表 {@code book}，存储图书的元数据信息和库存状态。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book")
public class Book extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 书名 —— 非空，长度不超过 200 字符 */
    private String title;
    /** 作者 —— 非空，长度不超过 100 字符 */
    private String author;
    /** ISBN 编号 —— 国际标准书号，唯一，非空，长度 13 或 17 字符 */
    private String isbn;
    /** 出版社 —— 非空，长度不超过 100 字符 */
    private String publisher;
    /** 出版日期 —— 可选 */
    private LocalDate publishDate;
    /** 图书简介 —— 可选，长度不超过 2000 字符 */
    private String description;
    /** 封面图片 URL —— MinIO 存储路径，可选，长度不超过 500 字符 */
    private String coverUrl;
    /** 总库存 —— 非空，默认 1，最小值 0 */
    private Integer stock;
    /** 可借库存 —— 非空，<= stock，已借出时扣减 */
    private Integer availableStock;
    /** 状态 —— {@link com.zjw.booknexus.enums.BookStatus} 枚举值，AVAILABLE / BORROWED / DAMAGED / LOST */
    private String status;
    /** 所属书架 ID —— 关联 {@link Bookshelf} 表，可选 */
    private Long bookshelfId;
}
