package com.zjw.booknexus.common;

import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 API 响应结果
 * <p>所有 Controller 接口统一返回此类型，保证前端响应格式一致。
 * 支持流体式链式调用， requestId 自动从 MDC 注入。</p>
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

    private int code;
    private String message;
    private T data;
    private String requestId;
    private String timestamp;

    public Result() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+08:00"));
        String reqId = MDC.get("requestId");
        if (reqId != null) {
            this.requestId = reqId;
        }
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> created(T data) {
        Result<T> r = new Result<>();
        r.code = 201;
        r.message = "created";
        r.data = data;
        return r;
    }

    public static <T> Result<T> failed(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> badRequest(String message) {
        return failed(400, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return failed(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return failed(403, message);
    }

    public static <T> Result<T> notFound(String message) {
        return failed(404, message);
    }

    public static <T> Result<T> conflict(String message) {
        return failed(409, message);
    }

    public static <T> Result<T> tooManyRequests(String message) {
        return failed(429, message);
    }

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

    public Result<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
