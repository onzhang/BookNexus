package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 公告表实体
 * <p>对应数据库表 {@code announcement}，存储管理员发布的系统公告，支持草稿与发布状态切换。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 公告标题 —— 非空，长度不超过 100 字符 */
    private String title;
    /** 公告正文 —— 非空，长度不超过 5000 字符 */
    private String content;
    /** 发布人 ID —— 关联 {@link User} 表，非空 */
    private Long publisherId;
    /** 是否已发布 —— 0:草稿，1:已发布，默认 0 */
    private Integer isPublished;
}
