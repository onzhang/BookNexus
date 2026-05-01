/**
 * 请求追踪 ID 拦截器
 * <p>为每个 HTTP 请求分配唯一的请求追踪 ID（Request ID），
 * 用于分布式链路追踪和日志关联。支持通过 {@code X-Request-Id} 请求头透传外部追踪 ID。</p>
 *
 * <p>工作流程：</p>
 * <ul>
 *   <li>{@link #preHandle} — 从请求头获取或生成 UUID，注入 MDC 和 request attribute</li>
 *   <li>{@link #postHandle} — 将追踪 ID 写入响应头 {@code X-Request-Id}，实现端到端透传</li>
 *   <li>{@link #afterCompletion} — 清理 MDC，防止内存泄漏</li>
 * </ul>
 *
 * <p>MDC Key 为 {@code requestId}，日志配置中通过 {@code %X{requestId}} 引用。</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

public class RequestIdInterceptor implements HandlerInterceptor {

    /** 请求追踪 ID 的 HTTP 头名称 */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    /** MDC 中存储追踪 ID 的 key */
    private static final String MDC_KEY = "requestId";

    /**
     * 前置处理 — 生成或获取请求追踪 ID
     * <p>优先使用客户端传入的 {@code X-Request-Id} 头（用于链路透传），
     * 若未传入则自动生成 32 位无横线 UUID。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  被调用的处理器对象
     * @return true 始终放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_KEY, requestId);
        request.setAttribute("requestId", requestId);
        return true;
    }

    /**
     * 后置处理 — 将追踪 ID 写入响应头
     * <p>在视图渲染前将当前请求的追踪 ID 设置到响应头中，
     * 便于客户端或调用方进行链路关联。</p>
     *
     * @param request       HTTP 请求
     * @param response      HTTP 响应
     * @param handler       被调用的处理器对象
     * @param modelAndView  模型和视图对象，可为 null
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        response.setHeader(REQUEST_ID_HEADER, MDC.get(MDC_KEY));
    }

    /**
     * 完成回调 — 清理 MDC 上下文
     * <p>确保请求处理完毕后移除 MDC 中的追踪 ID，防止线程池复用导致的上下文污染。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  被调用的处理器对象
     * @param ex       处理过程中发生的异常，无异常时为 null
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(MDC_KEY);
    }
}
