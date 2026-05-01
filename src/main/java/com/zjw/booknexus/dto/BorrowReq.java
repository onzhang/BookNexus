package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 借阅 / 归还书籍请求 DTO
 * <p>
 * 用于接收用户借书或还书操作时提交的请求数据，
 * 仅需提供目标书籍 ID 即可完成操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class BorrowReq {

    /** 书籍 ID（必填） */
    @NotNull(message = "书籍ID不能为空")
    private Long bookId;
}
