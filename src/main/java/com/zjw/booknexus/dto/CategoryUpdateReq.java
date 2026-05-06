package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 分类信息更新请求 DTO
 * <p>
 * 用于管理员更新分类信息时提交的修改数据，所有字段均为可选，
 * 仅对非空字段进行更新操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class CategoryUpdateReq {

    /** 分类名称 */
    private String name;

    /** 父分类 ID —— 顶级分类传 0 */
    private Long parentId;

    /** 排序序号 —— 同层级分类的显示顺序 */
    private Integer sortOrder;
}
