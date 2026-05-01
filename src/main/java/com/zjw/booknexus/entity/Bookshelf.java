package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bookshelf")
public class Bookshelf extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String location;
    private String description;
}
