package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类视图对象 VO
 * <p>
 * 用于前端分类详情和列表展示，包含分类基本信息、父分类信息和子分类列表。
 * 支持树形结构展示。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class CategoryVO {

    /** 分类 ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类 ID —— 顶级分类为 0 */
    private Long parentId;

    /** 父分类名称 */
    private String parentName;

    /** 排序序号 —— 同层级分类的显示顺序 */
    private Integer sortOrder;

    /** 子分类列表 —— 用于树形结构展示 */
    private List<CategoryVO> children;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
