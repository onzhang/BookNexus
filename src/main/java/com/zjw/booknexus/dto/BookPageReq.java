package com.zjw.booknexus.dto;

import lombok.Data;

@Data
public class BookPageReq {

    private Integer page = 1;

    private Integer size = 10;

    private String keyword;

    private String status;

    private Long bookshelfId;
}
