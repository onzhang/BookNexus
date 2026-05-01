/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 业务异常，携带错误码和描述信息，由全局异常处理器统一捕获
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.exception;

/**
 * 业务异常
 * <p>继承 RuntimeException，携带业务错误码，由 GlobalExceptionHandler 统一捕获并返回 Result。
 * 示例：throw new BusinessException(400, "该书已被借出");</p>
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码，与 HTTP 状态码对应 */
    private final int code;

    /**
     * 构造业务异常
     *
     * @param code    错误码（如 400、404、409）
     * @param message 错误描述
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（默认错误码 400）
     *
     * @param message 错误描述
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() { return code; }

}
