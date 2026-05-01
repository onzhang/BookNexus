/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 实体基类，提供公共审计字段（id、创建时间、更新时间、逻辑删除标记）
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.common;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>所有数据库实体继承此类，自动包含审计字段，由 MyBatis-Plus 自动填充。</p>
 */
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记（0=正常，1=已删除） */
    @TableLogic
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

}
