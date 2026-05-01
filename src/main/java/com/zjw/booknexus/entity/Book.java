package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book")
public class Book extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDate publishDate;
    private String description;
    private String coverUrl;
    private Integer stock;
    private Integer availableStock;
    private String status;
    private Long bookshelfId;
}
