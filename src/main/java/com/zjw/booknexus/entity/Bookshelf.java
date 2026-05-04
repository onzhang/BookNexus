package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 书架表实体
 * <p>对应数据库表 {@code bookshelf}，存储图书馆实体书架的编号和位置信息，用于图书物理定位。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bookshelf")
public class Bookshelf extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 书架名称/编号 —— 非空，唯一，长度不超过 50 字符 */
    private String name;
    /** 物理位置描述 —— 如"A区3排2层"，非空，长度不超过 200 字符 */
    private String location;
    /** 书架备注说明 —— 可选，长度不超过 500 字符 */
    private String description;
}
