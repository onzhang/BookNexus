/*
 * Copyright (c) 2026 BookNexus. All rights reserved.
 *
 * Sentinel 限流规则初始化器
 * 覆盖场景：核心接口 QPS 限流、降级策略配置
 *
 * @author 张俊文
 * @since 2026-04-30
 */
package com.zjw.booknexus.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.zjw.booknexus.common.Result;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流熔断规则初始化器
 * <p>应用启动时自动注册核心接口的限流规则（FlowRule）和降级规则（DegradeRule），
 * 保护图书查询、借阅、认证等高并发接口不被突发流量击穿。</p>
 *
 * <p><b>规则设计：</b></p>
 * <ul>
 *   <li><b>图书查询类</b>（bookPage / bookGetById）— QPS 100，读多写少，可适当放宽</li>
 *   <li><b>借阅操作类</b>（borrow / returnBook / renew / myBorrows）— QPS 50，写操作更严格</li>
 *   <li><b>认证类</b>（login / register / refresh）— QPS 30，防止暴力破解和刷注册</li>
 *   <li><b>用户管理类</b>（userPage / updateStatus）— QPS 30，管理端低频操作</li>
 *   <li><b>降级策略</b>— 异常比例超过 50% 且 10 秒内达到 5 次请求时触发熔断，熔断时长 10 秒</li>
 * </ul>
 *
 * <p><b>涉及中间件：</b>Sentinel 1.8.8（限流熔断框架）</p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Component
public class SentinelRuleInitializer {

    /**
     * 初始化限流与降级规则
     * <p>应用启动时自动执行，向 Sentinel 注册所有核心接口的流量控制和熔断保护规则。
     * 若规则注册失败会记录错误日志，但不影响应用启动。</p>
     */
    @PostConstruct
    public void initRules() {
        try {
            initFlowRules();
            initDegradeRules();
            log.info("[Sentinel] 限流与降级规则初始化完成，共注册 {} 条流控规则、{} 条降级规则",
                    FlowRuleManager.getRules().size(), DegradeRuleManager.getRules().size());
        } catch (Exception e) {
            log.error("[Sentinel] 规则初始化失败", e);
        }
    }

    /**
     * 注册流量控制规则（QPS 限流）
     * <p>为各核心资源定义每秒最大请求数，超过阈值时直接拒绝请求并触发降级逻辑。
     * 控制效果统一使用快速失败（直接拒绝），降低系统负载。</p>
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // ==================== 图书查询类 ====================
        // 图书分页查询：QPS 100，列表页访问频繁
        rules.add(buildFlowRule("bookPage", 100));
        // 图书详情查询：QPS 100，详情页访问频繁
        rules.add(buildFlowRule("bookGetById", 100));
        // 图书创建：QPS 20，管理端低频写操作
        rules.add(buildFlowRule("bookCreate", 20));
        // 图书更新：QPS 20，管理端低频写操作
        rules.add(buildFlowRule("bookUpdate", 20));
        // 图书删除：QPS 10，管理端极低频操作
        rules.add(buildFlowRule("bookDelete", 10));

        // ==================== 借阅操作类 ====================
        // 借阅图书：QPS 50，核心写操作
        rules.add(buildFlowRule("borrow", 50));
        // 归还图书：QPS 50，核心写操作
        rules.add(buildFlowRule("returnBook", 50));
        // 续借图书：QPS 30，低频写操作
        rules.add(buildFlowRule("renew", 30));
        // 我的借阅列表：QPS 50，查询操作
        rules.add(buildFlowRule("myBorrows", 50));

        // ==================== 认证类 ====================
        // 用户登录：QPS 30，防止暴力破解
        rules.add(buildFlowRule("login", 30));
        // 用户注册：QPS 20，防止恶意刷注册
        rules.add(buildFlowRule("register", 20));
        // 令牌刷新：QPS 30，常规频率
        rules.add(buildFlowRule("refresh", 30));

        // ==================== 用户管理类 ====================
        // 用户分页查询：QPS 30，管理端低频操作
        rules.add(buildFlowRule("userPage", 30));
        // 用户状态更新：QPS 20，管理端低频操作
        rules.add(buildFlowRule("updateStatus", 20));

        FlowRuleManager.loadRules(rules);
    }

    /**
     * 注册降级规则（熔断策略）
     * <p>当接口异常比例超过阈值时自动熔断，防止故障扩散。
     * 熔断时长 10 秒，恢复期后进入半开状态探测服务恢复情况。</p>
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 图书查询降级：异常比例超过 50%，10 秒内达到 5 次请求触发熔断
        rules.add(buildDegradeRule("bookPage"));
        rules.add(buildDegradeRule("bookGetById"));

        // 借阅操作降级
        rules.add(buildDegradeRule("borrow"));
        rules.add(buildDegradeRule("returnBook"));

        // 认证降级
        rules.add(buildDegradeRule("login"));
        rules.add(buildDegradeRule("register"));

        DegradeRuleManager.loadRules(rules);
    }

    /**
     * 构建单条流控规则
     *
     * @param resource 资源名称（与 {@code @SentinelResource} 的 value 对应）
     * @param qps      每秒最大请求数阈值
     * @return FlowRule 流控规则实例
     */
    private FlowRule buildFlowRule(String resource, int qps) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        return rule;
    }

    /**
     * 构建单条降级规则
     *
     * @param resource 资源名称（与 {@code @SentinelResource} 的 value 对应）
     * @return DegradeRule 降级规则实例
     */
    private DegradeRule buildDegradeRule(String resource) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        rule.setCount(0.5);          // 异常比例阈值 50%
        rule.setTimeWindow(10);      // 熔断时长 10 秒
        rule.setStatIntervalMs(10000); // 统计时长 10 秒
        rule.setMinRequestAmount(5);   // 最小请求数 5
        return rule;
    }

    /**
     * 通用限流降级 fallback 方法
     * <p>当请求被 Sentinel 限流或熔断时，返回统一的友好提示响应。
     * 要求 fallback 方法签名与原方法一致，并额外接收一个 {@link BlockException} 参数。</p>
     *
     * @param ex BlockException 异常实例
     * @return 统一降级响应
     */
    public static Result<Void> fallback(BlockException ex) {
        log.warn("[Sentinel] 请求被限流或熔断: {}", ex.getRule().getResource());
        return Result.tooManyRequests("请求过于频繁，请稍后再试");
    }
}
