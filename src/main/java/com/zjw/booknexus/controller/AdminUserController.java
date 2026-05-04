package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.UserPageReq;
import com.zjw.booknexus.dto.UserUpdateReq;
import com.zjw.booknexus.service.UserService;
import com.zjw.booknexus.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端用户管理控制器，提供用户信息的查询和管理功能。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/users。
 * 包含用户分页查询、详情查看、信息编辑和状态管理功能。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 用户分页查询接口。
     * <p>
     * GET /api/v1/admin/users
     * 支持按关键词（用户名/邮箱）进行模糊搜索，
     * 结果按创建时间倒序排列，便于管理员管理用户列表。
     * </p>
     *
     * @param req 分页查询参数，包含页码、每页大小、关键词等筛选条件
     * @return 用户信息的分页列表统一响应
     */
    @GetMapping
    public Result<PageResult<UserVO>> page(@ModelAttribute UserPageReq req) {
        return Result.success(userService.page(req));
    }

    /**
     * 用户详情查询接口。
     * <p>
     * GET /api/v1/admin/users/{id}
     * 根据用户 ID 查询用户的详细信息，包含角色、状态、联系方式等。
     * </p>
     *
     * @param id 用户 ID
     * @return 用户详细信息统一响应
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 更新用户信息接口。
     * <p>
     * PUT /api/v1/admin/users/{id}
     * 管理员修改指定用户的邮箱、电话和状态信息。
     * 注意：管理员不能禁用自己（当前登录用户），防止误操作导致无法登录。
     * </p>
     *
     * @param id  用户 ID
     * @param req 用户更新请求体，包含需要更新的字段
     * @return 统一成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateReq req) {
        userService.update(id, req);
        return Result.success();
    }

    /**
     * 启用/禁用用户接口。
     * <p>
     * PUT /api/v1/admin/users/{id}/status
     * 管理员更新指定用户的状态（ENABLED/DISABLED）。
     * 注意：管理员不能禁用自己（当前登录用户），防止误操作导致账户被锁定。
     * </p>
     *
     * @param id   用户 ID
     * @param body 状态请求体，包含 status 字段（ENABLED 或 DISABLED）
     * @return 统一成功响应
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.updateStatus(id, body.get("status"));
        return Result.success();
    }
}
