package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图书-分类关联表实体
 * <p>对应数据库表 {@code book_category_rel}，实现图书与分类的多对多关系映射。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@TableName("book_category_rel")
public class BookCategoryRel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID —— 自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 图书 ID —— 关联 {@link Book} 表，非空 */
    private Long bookId;
    /** 分类 ID —— 关联 {@link Category} 表，非空 */
    private Long categoryId;
}
