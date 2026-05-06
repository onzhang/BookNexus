package com.zjw.booknexus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 新增书籍请求 DTO
 * <p>
 * 用于接收前端新增书籍时提交的完整表单数据，包含书籍基本信息、
 * 所属书架及分类关联信息。ISBN、书名、作者、出版社为必填项。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BookCreateReq {

    /** ISBN 编号（必填） */
    @NotBlank(message = "ISBN不能为空")
    @Pattern(regexp = "^(\\d{13}|\\d{17})$", message = "ISBN格式不正确")
    private String isbn;

    /** 书名（必填） */
    @NotBlank(message = "书名不能为空")
    private String title;

    /** 作者（必填） */
    @NotBlank(message = "作者不能为空")
    private String author;

    /** 出版社（必填） */
    @NotBlank(message = "出版社不能为空")
    private String publisher;

    /** 书籍简介 */
    private String description;

    /** 封面图片 URL */
    private String coverUrl;

    /** 出版日期 */
    @PastOrPresent
    private LocalDate publishedDate;

    /** 总库存量（默认1） */
    @Min(value = 1, message = "库存不能小于1")
    private Integer stock = 1;

    /** 所属书架 ID */
    private Long bookshelfId;

    /** 关联的分类 ID 列表 */
    private List<Long> categoryIds;
}
