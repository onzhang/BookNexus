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
 * <p>继承 {@link RuntimeException}，携带业务错误码和描述信息。
 * 由 {@link com.zjw.booknexus.exception.GlobalExceptionHandler GlobalExceptionHandler}
 * 统一捕获并转换为 {@link com.zjw.booknexus.common.Result Result} 响应。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * throw new BusinessException(400, "该书已被借出");
 * throw new BusinessException(ErrorCode.BOOK_NOT_FOUND, "图书不存在");
 * </pre>
 *
 * @author 张俊文
 * @since 2026-04-30
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
