package com.zjw.booknexus.exception;

import com.zjw.booknexus.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>使用 {@code @RestControllerAdvice} 统一捕获 Controller 层及各层抛出的异常，
 * 转换为标准 {@link com.zjw.booknexus.common.Result Result} 响应格式。</p>
 *
 * <p>覆盖的异常类型：</p>
 * <ul>
 *   <li>{@link BusinessException} — 业务异常，动态映射 HTTP 状态码</li>
 *   <li>{@link org.springframework.web.bind.MethodArgumentNotValidException MethodArgumentNotValidException} — {@code @RequestBody} 参数校验失败（400）</li>
 *   <li>{@link HttpMessageNotReadableException} — 请求体解析失败（400）</li>
 *   <li>{@link jakarta.validation.ConstraintViolationException ConstraintViolationException} — {@code @RequestParam/@PathVariable} 参数校验失败（400）</li>
 *   <li>{@link Exception} — 兜底捕获，返回 500 Internal Server Error</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(Result.failed(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Result.badRequest(msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Result.badRequest("请求体格式错误"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(Result.badRequest(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("internal server error"));
    }

}
