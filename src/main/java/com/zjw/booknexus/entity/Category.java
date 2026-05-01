package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 分类表实体
 * <p>对应数据库表 {@code category}，存储图书分类信息，支持多级树形结构（通过 parentId 自关联）。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类名称 —— 非空，唯一，长度不超过 50 字符 */
    private String name;
    /** 父分类 ID —— 自关联 {@code id}，顶级分类该值为 0 */
    private Long parentId;
    /** 排序序号 —— 同层级分类的显示顺序，默认 0 */
    private Integer sortOrder;
}
