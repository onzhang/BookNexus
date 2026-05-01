package com.zjw.booknexus.dto;

import lombok.Data;

@Data
public class UserPageReq {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
}
