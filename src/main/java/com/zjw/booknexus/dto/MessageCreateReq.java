package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交留言请求 DTO
 * <p>
 * 用于接收前端用户提交的留言/建议内容。
 * 留言内容为必填项。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class MessageCreateReq {

    /** 留言内容（必填） */
    @NotBlank(message = "留言内容不能为空")
    private String content;
}
