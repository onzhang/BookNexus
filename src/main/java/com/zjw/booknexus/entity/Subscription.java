package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订阅表实体
 * <p>对应数据库表 {@code subscription}，存储用户对图书的订阅关系，当图书可借时推送通知。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@TableName("subscription")
public class Subscription implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID —— 自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID —— 关联 {@link User} 表，非空 */
    private Long userId;
    /** 图书 ID —— 关联 {@link Book} 表，非空 */
    private Long bookId;
    /** 是否活跃订阅 —— 0:已取消，1:订阅中，默认 1 */
    private Integer isActive;
    /** 订阅时间 —— 非空，默认当前时间 */
    private LocalDateTime createdAt;
    /** 更新时间 —— 取消订阅时记录修改时间 */
    private LocalDateTime updatedAt;
}
