package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏表实体
 * <p>对应数据库表 {@code favorite}，存储用户收藏的图书记录，支持快速访问喜爱的书籍。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@TableName("favorite")
public class Favorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID —— 自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID —— 关联 {@link User} 表，非空 */
    private Long userId;
    /** 图书 ID —— 关联 {@link Book} 表，非空 */
    private Long bookId;
    /** 收藏时间 —— 非空，默认当前时间 */
    private LocalDateTime createdAt;
}
