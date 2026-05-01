package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookVO {

    private Long id;
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
    private String bookshelfName;
    private List<String> categoryNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
