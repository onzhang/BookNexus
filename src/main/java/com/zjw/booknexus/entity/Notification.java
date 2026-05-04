package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 通知表实体
 * <p>对应数据库表 {@code notification}，存储系统推送给用户的站内通知，包括系统通知、订阅通知和逾期催还。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 接收用户 ID —— 关联 {@link User} 表，非空 */
    private Long userId;
    /** 通知类型 —— {@link com.zjw.booknexus.enums.NotificationType} 枚举值，SYSTEM / SUBSCRIPTION / OVERDUE */
    private String type;
    /** 通知标题 —— 非空，长度不超过 100 字符 */
    private String title;
    /** 通知正文 —— 非空，长度不超过 2000 字符 */
    private String content;
    /** 是否已读 —— 0:未读，1:已读，默认 0 */
    private Integer isRead;
}
