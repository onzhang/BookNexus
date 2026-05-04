package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 书籍视图对象 VO
 * <p>
 * 用于前端书籍详情和列表展示，聚合书籍基本信息、库存信息、
 * 所属书架名称和关联分类名称等数据。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BookVO {

    /** 书籍 ID */
    private Long id;

    /** 书名 */
    private String title;

    /** 作者 */
    private String author;

    /** ISBN 编号 */
    private String isbn;

    /** 出版社 */
    private String publisher;

    /** 出版日期 */
    private LocalDate publishDate;

    /** 书籍简介 */
    private String description;

    /** 封面图片 URL */
    private String coverUrl;

    /** 总库存数量 */
    private Integer stock;

    /** 当前可借库存数量 */
    private Integer availableStock;

    /** 书籍状态（AVAILABLE / BORROWED / DAMAGED / LOST） */
    private String status;

    /** 所属书架 ID */
    private Long bookshelfId;

    /** 所属书架名称 */
    private String bookshelfName;

    /** 关联的分类名称列表 */
    private List<String> categoryNames;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
