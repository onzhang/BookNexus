package com.zjw.booknexus.dto;

import lombok.Data;

@Data
public class AdminBorrowPageReq {

    private Integer page = 1;
    private Integer size = 10;
    private String status;
    private Long userId;
    private String keyword;
}
