package com.zjw.booknexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书架视图对象 VO
 * <p>
 * 用于前端书架详情和列表展示，包含书架基本信息、位置和描述。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Data
public class BookshelfVO {

    /** 书架 ID */
    private Long id;

    /** 书架名称/编号 */
    private String name;

    /** 物理位置描述 */
    private String location;

    /** 书架备注说明 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
