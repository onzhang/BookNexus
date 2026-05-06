package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏 / 取消收藏请求 DTO
 * <p>
 * 用于接收用户收藏或取消收藏操作时提交的请求数据，
 * 仅需提供目标书籍 ID 即可完成操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class FavoriteReq {

    /** 书籍 ID（必填） */
    @NotNull(message = "书籍ID不能为空")
    private Long bookId;
}
