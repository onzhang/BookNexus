/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * 全局异常处理器，统一捕获并返回标准 Result 响应
 * 覆盖：业务异常、参数校验异常、未知系统异常
 *
 * @author 张俊文
 * @since 2026-04-29
 */
package com.zjw.booknexus.exception;

import com.zjw.booknexus.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>使用 @RestControllerAdvice 统一捕获各层异常，转换为标准 Result 响应格式。
 * 覆盖业务异常、参数校验异常、未知系统异常三大类。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * <p>Service 层抛出 BusinessException 时被此方法捕获，返回对应的错误码和提示。</p>
     *
     * @param e 业务异常
     * @return Result 响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(Result.failed(e.getCode(), e.getMessage()));
    }

    /**
     * 处理 @RequestBody 参数校验异常
     * <p>当 @Valid 或 @Validated 标注的请求体参数校验失败时触发，
     * 收集所有字段错误信息并用逗号拼接返回。</p>
     *
     * @param e 参数校验异常
     * @return Result 响应（400 Bad Request）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Result.badRequest(msg));
    }

    /**
     * 处理 @RequestParam/@PathVariable 参数校验异常
     * <p>当控制器层使用 @Validated 标注的单个参数校验失败时触发，
     * 直接返回 ConstraintViolationException 的错误信息。</p>
     *
     * @param e 参数约束违反异常
     * @return Result 响应（400 Bad Request）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(Result.badRequest(e.getMessage()));
    }

    /**
     * 处理未知系统异常
     * <p>捕获所有未被上述 Handler 覆盖的异常，记录错误堆栈后
     * 返回 500 Internal Server Error 的统一响应。</p>
     *
     * @param e 未知异常
     * @return Result 响应（500 Internal Server Error）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("internal server error"));
    }

}
