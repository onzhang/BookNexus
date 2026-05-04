package com.zjw.booknexus.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 更新书籍信息请求 DTO
 * <p>
 * 用于接收前端更新书籍时提交的修改数据，所有字段均为可选，
 * 仅对非空字段进行更新操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BookUpdateReq {

    /** ISBN 编号 */
    private String isbn;

    /** 书名 */
    private String title;

    /** 作者 */
    private String author;

    /** 出版社 */
    private String publisher;

    /** 书籍简介 */
    private String description;

    /** 封面图片 URL */
    private String coverUrl;

    /** 出版日期 */
    private LocalDate publishedDate;

    /** 所属书架 ID */
    private Long bookshelfId;

    /** 关联的分类 ID 列表 */
    private List<Long> categoryIds;
}
