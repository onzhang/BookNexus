package com.zjw.booknexus.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookUpdateReq {

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private String description;

    private String coverUrl;

    private LocalDate publishedDate;

    private Long bookshelfId;

    private List<Long> categoryIds;
}
