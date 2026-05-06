package com.zjw.booknexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增书架请求 DTO
 * <p>
 * 用于接收前端新增书架时提交的表单数据，包含书架名称、位置和描述信息。
 * 书架名称为必填项且需保证唯一性。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class BookshelfCreateReq {

    /** 书架名称/编号（必填） */
    @NotBlank(message = "书架名称不能为空")
    private String name;

    /** 物理位置描述 */
    private String location;

    /** 书架备注说明 */
    private String description;
}
