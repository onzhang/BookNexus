package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增公告请求 DTO
 * <p>
 * 用于接收前端提交的新增公告表单数据，包含公告标题、正文和发布状态。
 * 标题和正文为必填项。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class AnnouncementCreateReq {

    /** 公告标题（必填） */
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /** 公告正文（必填） */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** 是否发布：0=草稿，1=已发布，默认 0 */
    private Integer isPublished = 0;
}
