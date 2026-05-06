package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 更新公告请求 DTO
 * <p>
 * 用于接收前端提交的公告更新表单数据，支持部分字段更新。
 * 所有字段均为可选，仅更新非空字段。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class AnnouncementUpdateReq {

    /** 公告标题 */
    private String title;

    /** 公告正文 */
    private String content;

    /** 是否发布：0=草稿，1=已发布 */
    private Integer isPublished;
}
