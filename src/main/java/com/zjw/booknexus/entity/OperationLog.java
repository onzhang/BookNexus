package com.zjw.booknexus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表实体
 * <p>对应数据库表 {@code operation_log}，记录管理员和用户的关键操作行为，用于审计追溯。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID —— 自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人标识 —— 用户名或系统，非空 */
    private String operator;
    /** 操作动作 —— 如 BORROW / RETURN / CREATE_BOOK / DELETE_USER 等 */
    private String action;
    /** 操作目标类型 —— 如 Book / User / BorrowRecord 等 */
    private String targetType;
    /** 操作目标 ID —— 对应的业务记录主键 */
    private Long targetId;
    /** 操作详情 —— JSON 格式，记录操作前后的关键字段变化 */
    private String detail;
    /** 请求来源 IP 地址 —— 用于安全审计 */
    private String ip;
    /** 操作结果 —— SUCCESS / FAILED */
    private String result;
    /** 操作时间 —— 非空，默认当前时间 */
    private LocalDateTime createdAt;
}
