package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增分类请求 DTO
 * <p>
 * 用于接收前端新增分类时提交的表单数据，包含分类名称、父分类 ID 和排序序号。
 * 分类名称为必填项且需保证唯一性。parentId 为 0 表示顶级分类。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class CategoryCreateReq {

    /** 分类名称（必填） */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 父分类 ID —— 顶级分类传 0 */
    private Long parentId = 0L;

    /** 排序序号 —— 同层级分类的显示顺序 */
    private Integer sortOrder = 0;
}
