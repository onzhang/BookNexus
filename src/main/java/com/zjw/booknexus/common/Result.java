/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 统一 API 响应结果，包含状态码、消息、数据、请求 ID 和时间戳
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 API 响应结果
 * <p>所有 Controller 接口统一返回此类型，保证前端响应格式一致。
 * 支持流体式链式调用，可通过 {@link #requestId(String)} 注入请求追踪 ID。</p>
 *
 * <p>响应示例：</p>
 * <pre>
 * {
 *   "code": 200,
 *   "message": "ok",
 *   "data": { ... },
 *   "requestId": "a1b2c3d4e5f6",
 *   "timestamp": "2026-04-30T10:30:00+08:00"
 * }
 * </pre>
 *
 * @param <T> 响应数据类型
 * @author 张俊文
 * @since 2026-04-30
 */
public class Result<T> {

    /** 状态码，200 表示成功 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 请求追踪 ID，用于链路排查 */
    private String requestId;

    /** 响应时间戳，格式：yyyy-MM-dd'T'HH:mm:ss+08:00 */
    private String timestamp;

    /** 默认构造器，自动生成时间戳 */
    public Result() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+08:00"));
    }

    /**
     * 操作成功（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result 实例，code=200
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "ok";
        r.data = data;
        return r;
    }

    /**
     * 操作成功（无数据）
     *
     * @param <T> 数据类型
     * @return Result 实例，code=200，data=null
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建成功（资源新建后返回）
     *
     * @param data 新创建的资源数据
     * @param <T>  数据类型
     * @return Result 实例，code=201
     */
    public static <T> Result<T> created(T data) {
        Result<T> r = new Result<>();
        r.code = 201;
        r.message = "created";
        r.data = data;
        return r;
    }

    /**
     * 操作失败（自定义错误码）
     *
     * @param code    错误码
     * @param message 错误描述
     * @param <T>     数据类型
     * @return Result 实例
     */
    public static <T> Result<T> failed(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    /** 400 参数错误 */
    public static <T> Result<T> badRequest(String message) {
        return failed(400, message);
    }

    /** 401 未登录 / Token 过期 */
    public static <T> Result<T> unauthorized(String message) {
        return failed(401, message);
    }

    /** 403 无权限 */
    public static <T> Result<T> forbidden(String message) {
        return failed(403, message);
    }

    /** 404 资源不存在 */
    public static <T> Result<T> notFound(String message) {
        return failed(404, message);
    }

    /** 409 数据冲突（如重复借阅） */
    public static <T> Result<T> conflict(String message) {
        return failed(409, message);
    }

    /** 429 请求过于频繁（Sentinel 限流触发） */
    public static <T> Result<T> tooManyRequests(String message) {
        return failed(429, message);
    }

    /** 500 服务器内部错误 */
    public static <T> Result<T> error(String message) {
        return failed(500, message);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    /**
     * 设置请求追踪 ID（流体式调用，支持链式语法）
     *
     * @param requestId 请求追踪 ID
     * @return 当前 Result 实例
     */
    public Result<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

}
