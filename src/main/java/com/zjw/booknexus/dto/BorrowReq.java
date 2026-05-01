package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowReq {

    @NotNull(message = "书籍ID不能为空")
    private Long bookId;
}
