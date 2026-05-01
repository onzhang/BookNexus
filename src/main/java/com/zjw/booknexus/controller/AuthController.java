package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RefreshReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.service.AuthService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器，提供用户认证相关的 RESTful API。
 * <p>
 * 处理用户注册、登录、令牌刷新以及获取当前登录用户信息等认证流程。
 * 公共接口前缀为 /api/v1/public/auth，用户接口前缀为 /api/v1/user/auth。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册接口。
     * <p>
     * POST /api/v1/public/auth/register
     * 接收用户注册信息（用户名、密码、邮箱），创建新用户并返回访问令牌和刷新令牌。
     * 注册成功后自动分配 USER 角色和 ENABLED 状态。
     * </p>
     *
     * @param req 注册请求体，包含用户名、密码、邮箱等信息
     * @return 包含访问令牌、刷新令牌及用户基本信息的统一响应
     */
    @PostMapping("/public/auth/register")
    public Result<LoginResp> register(@Valid @RequestBody RegisterReq req) {
        return Result.created(authService.register(req));
    }

    /**
     * 用户登录接口。
     * <p>
     * POST /api/v1/public/auth/login
     * 根据用户名和密码进行身份认证，认证通过后返回访问令牌和刷新令牌。
     * 已禁用账户无法登录。
     * </p>
     *
     * @param req 登录请求体，包含用户名和密码
     * @return 包含访问令牌、刷新令牌及用户基本信息的统一响应
     */
    @PostMapping("/public/auth/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.login(req));
    }

    /**
     * 刷新访问令牌接口。
     * <p>
     * POST /api/v1/public/auth/refresh
     * 使用有效的刷新令牌换取新的访问令牌和刷新令牌。
     * 刷新令牌具有较长的有效期，用于在访问令牌过期后无感续期。
     * </p>
     *
     * @param req 刷新请求体，包含刷新令牌
     * @return 包含新生成的访问令牌、刷新令牌及用户基本信息的统一响应
     */
    @PostMapping("/public/auth/refresh")
    public Result<LoginResp> refresh(@Valid @RequestBody RefreshReq req) {
        return Result.success(authService.refresh(req));
    }

    /**
     * 获取当前登录用户信息接口。
     * <p>
     * GET /api/v1/user/auth/me
     * 根据当前请求上下文中解析的用户 ID 查询用户详细信息。
     * 需要携带有效的访问令牌进行身份认证。
     * </p>
     *
     * @return 当前登录用户的详细信息统一响应
     */
    @GetMapping("/user/auth/me")
    public Result<UserVO> me() {
        return Result.success(authService.getCurrentUser(UserContext.getUserId()));
    }
}
