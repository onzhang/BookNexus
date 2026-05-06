package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 回复留言请求 DTO
 * <p>
 * 用于接收管理员对留言进行回复的表单数据。
 * 回复内容为必填项。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class MessageReplyReq {

    /** 管理员回复内容（必填） */
    @NotBlank(message = "回复内容不能为空")
    private String reply;
}
