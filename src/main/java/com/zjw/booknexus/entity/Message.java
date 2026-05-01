package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjw.booknexus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String content;
    private String reply;
    private LocalDateTime replyAt;
    private Long replierId;
}
