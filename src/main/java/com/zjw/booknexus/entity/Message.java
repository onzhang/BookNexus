package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 消息表实体
 * <p>对应数据库表 {@code message}，存储用户向管理员发送的站内信以及管理员的回复内容。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 发送用户 ID —— 关联 {@link User} 表，非空 */
    private Long userId;
    /** 消息内容 —— 用户发送的咨询内容，非空，长度不超过 2000 字符 */
    private String content;
    /** 管理员回复 —— 可选，回复前为 NULL */
    private String reply;
    /** 回复时间 —— 管理员回复时写入 */
    private LocalDateTime replyAt;
    /** 回复人 ID —— 关联 {@link User} 表，回复的管理员用户 ID */
    private Long replierId;
}
