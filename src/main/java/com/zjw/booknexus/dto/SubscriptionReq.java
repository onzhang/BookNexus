package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订阅 / 取消订阅请求 DTO
 * <p>
 * 用于接收用户订阅或取消订阅图书归还通知时提交的请求数据，
 * 仅需提供目标书籍 ID 即可完成操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class SubscriptionReq {

    /** 书籍 ID（必填） */
    @NotNull(message = "书籍ID不能为空")
    private Long bookId;
}
