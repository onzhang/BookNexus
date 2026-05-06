package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 书架信息更新请求 DTO
 * <p>
 * 用于管理员更新书架信息时提交的修改数据，所有字段均为可选，
 * 仅对非空字段进行更新操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class BookshelfUpdateReq {

    /** 书架名称/编号 */
    private String name;

    /** 物理位置描述 */
    private String location;

    /** 书架备注说明 */
    private String description;
}
