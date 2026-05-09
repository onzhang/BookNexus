package com.zjw.booknexus.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjw.booknexus.config.RabbitMQConfig;
import com.zjw.booknexus.entity.OperationLog;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.mapper.UserMapper;
import com.zjw.booknexus.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.dto.RefreshReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 操作日志 AOP 切面
 * <p>
 * 拦截所有 Controller 的写操作（POST / PUT / DELETE），根据业务规则决定是否记录操作日志：
 * <ul>
 *   <li>管理端（/api/v1/admin/**）所有写操作全部记录</li>
 *   <li>用户端（/api/v1/user/**）仅记录：借阅申请、续借、取消订阅</li>
 * </ul>
 * 日志信息通过 RabbitMQ 异步投递到 {@code log.operation.queue}，由 {@link com.zjw.booknexus.consumer.LogOperationConsumer}
 * 消费并持久化到数据库，降低请求链路延迟。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final RabbitTemplate rabbitTemplate;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private static final Pattern TARGET_ID_PATTERN = Pattern.compile("/(\\d+)(?:/|$)");

    /**
     * Controller 写操作切入点
     * <p>匹配 com.zjw.booknexus.controller 包下所有带有 PostMapping / PutMapping / DeleteMapping 的方法。</p>
     */
    @Pointcut("execution(* com.zjw.booknexus.controller..*(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void controllerWriteOps() {}

    /**
     * 环绕通知：记录操作日志并异步发送到 MQ。
     *
     * @param joinPoint 连接点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常
     */
    @Around("controllerWriteOps()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attrs.getRequest();
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!shouldLog(uri, method)) {
            return joinPoint.proceed();
        }

        OperationLog opLog = new OperationLog();
        opLog.setOperator(resolveOperator());
        opLog.setAction(resolveAction(uri, method));
        opLog.setTargetType(resolveTargetType(uri));
        opLog.setTargetId(resolveTargetId(uri));
        opLog.setIp(getClientIp(request));
        opLog.setCreatedAt(LocalDateTime.now());

        Object result = null;
        boolean success = true;
        try {
            result = joinPoint.proceed();
            opLog.setResult("SUCCESS");
        } catch (Exception e) {
            success = false;
            opLog.setResult("FAILED");
            throw e;
        } finally {
            opLog.setDetail(buildDetail(uri, method, joinPoint.getArgs(), success));
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.LOG_EXCHANGE, RabbitMQConfig.LOG_ROUTING, opLog);
            } catch (Exception ex) {
                log.error("操作日志发送 MQ 失败", ex);
            }
        }
        return result;
    }

    /**
     * 判断当前请求是否需要记录操作日志。
     *
     * @param uri    请求 URI
     * @param method HTTP 方法
     * @return true = 需要记录
     */
    private boolean shouldLog(String uri, String method) {
        if (uri.startsWith("/api/v1/admin/")) {
            return true;
        }
        if (uri.startsWith("/api/v1/user/")) {
            // 借阅申请
            if ("POST".equals(method) && uri.equals("/api/v1/user/borrows")) {
                return true;
            }
            // 续借
            if ("PUT".equals(method) && uri.matches("/api/v1/user/borrows/\\d+/renew")) {
                return true;
            }
            // 取消订阅
            if ("DELETE".equals(method) && uri.matches("/api/v1/user/subscriptions/\\d+")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析操作人用户名。
     *
     * @return 用户名；若未登录则返回 {@code anonymous}
     */
    private String resolveOperator() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return "anonymous";
        }
        User user = userMapper.selectById(userId);
        return user != null ? user.getUsername() : "unknown";
    }

    /**
     * 根据 URI 和 HTTP 方法推断操作动作。
     *
     * @param uri    请求 URI
     * @param method HTTP 方法
     * @return 动作描述，如 CREATE_Book、UPDATE_Book、RENEW_BorrowRecord 等
     */
    private String resolveAction(String uri, String method) {
        String targetType = resolveTargetType(uri);
        return switch (method) {
            case "POST" -> "CREATE_" + targetType;
            case "PUT" -> {
                if (uri.contains("/renew")) {
                    yield "RENEW_" + targetType;
                }
                if (uri.contains("/return")) {
                    yield "RETURN_" + targetType;
                }
                yield "UPDATE_" + targetType;
            }
            case "DELETE" -> "DELETE_" + targetType;
            default -> method + "_" + targetType;
        };
    }

    /**
     * 根据 URI 推断操作目标类型。
     *
     * @param uri 请求 URI
     * @return 目标类型，如 Book、User、BorrowRecord 等
     */
    private String resolveTargetType(String uri) {
        if (uri.contains("/books/") || uri.endsWith("/books")) {
            return "Book";
        }
        if (uri.contains("/users/") || uri.endsWith("/users")) {
            return "User";
        }
        if (uri.contains("/borrows/") || uri.endsWith("/borrows")) {
            return "BorrowRecord";
        }
        if (uri.contains("/subscriptions/") || uri.endsWith("/subscriptions")) {
            return "Subscription";
        }
        if (uri.contains("/categories/") || uri.endsWith("/categories")) {
            return "Category";
        }
        if (uri.contains("/bookshelves/") || uri.endsWith("/bookshelves")) {
            return "Bookshelf";
        }
        if (uri.contains("/favorites/") || uri.endsWith("/favorites")) {
            return "Favorite";
        }
        return "Unknown";
    }

    /**
     * 从 URI 中提取最后一个数字 ID 作为目标 ID。
     *
     * @param uri 请求 URI
     * @return 目标 ID；若不存在则返回 {@code null}
     */
    private Long resolveTargetId(String uri) {
        Matcher matcher = TARGET_ID_PATTERN.matcher(uri);
        Long lastId = null;
        while (matcher.find()) {
            lastId = Long.valueOf(matcher.group(1));
        }
        return lastId;
    }

    /**
     * 获取客户端真实 IP 地址。
     *
     * @param request HTTP 请求
     * @return IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 构建操作详情 JSON。
     *
     * @param uri     请求 URI
     * @param method  HTTP 方法
     * @param args    方法参数
     * @param success 是否执行成功
     * @return JSON 字符串
     */
    private String buildDetail(String uri, String method, Object[] args, boolean success) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uri", uri);
        map.put("method", method);
        map.put("success", success);
        if (args != null && args.length > 0) {
            List<Object> params = new ArrayList<>();
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                String className = arg.getClass().getName();
                // 过滤 Servlet 相关对象、校验结果对象和包含敏感字段的认证 DTO
                if (className.startsWith("jakarta.servlet.")
                        || className.startsWith("org.springframework.validation.")
                        || arg instanceof com.zjw.booknexus.dto.LoginReq
                        || arg instanceof com.zjw.booknexus.dto.RegisterReq
                        || arg instanceof com.zjw.booknexus.dto.RefreshReq) {
                    continue;
                }
                params.add(arg);
            }
            if (!params.isEmpty()) {
                map.put("params", params);
            }
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"uri\":\"" + uri + "\",\"method\":\"" + method + "\"}";
        }
    }
}
