package com.zjw.booknexus.service;

import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RefreshReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.vo.UserVO;

/**
 * 认证服务接口，定义用户认证相关的核心业务逻辑。
 * <p>
 * 包含用户注册、登录认证、令牌刷新以及当前用户信息查询等功能。
 * 实现类需处理密码加密、JWT 令牌生成、Redis 存储刷新令牌等认证流程。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface AuthService {

    /**
     * 用户注册。
     * <p>
     * 校验用户名和邮箱的唯一性，对密码进行 BCrypt 加密后持久化用户信息，
     * 注册成功后自动生成并返回访问令牌和刷新令牌。
     * </p>
     *
     * @param req 注册请求，包含用户名、密码、邮箱等信息
     * @return 登录响应，包含访问令牌、刷新令牌及用户基本信息
     * @throws com.zjw.booknexus.exception.BusinessException 当用户名或邮箱已存在时抛出
     */
    LoginResp register(RegisterReq req);

    /**
     * 用户登录认证。
     * <p>
     * 根据用户名查询用户，校验密码匹配性并检查账户状态，
     * 认证通过后生成并返回访问令牌和刷新令牌。
     * </p>
     *
     * @param req 登录请求，包含用户名和密码
     * @return 登录响应，包含访问令牌、刷新令牌及用户基本信息
     * @throws com.zjw.booknexus.exception.BusinessException 当用户名不存在、密码错误或账户已禁用时抛出
     */
    LoginResp login(LoginReq req);

    /**
     * 刷新访问令牌。
     * <p>
     * 校验刷新令牌的有效性和类型，从令牌中解析用户信息并发起新令牌。
     * 刷新令牌通过 Redis 存储以实现主动失效能力。
     * </p>
     *
     * @param req 刷新请求，包含当前有效的刷新令牌
     * @return 登录响应，包含新生成的访问令牌、刷新令牌及用户基本信息
     * @throws com.zjw.booknexus.exception.BusinessException 当刷新令牌无效、过期或用户已被禁用时抛出
     */
    LoginResp refresh(RefreshReq req);

    /**
     * 获取当前登录用户信息。
     * <p>
     * 根据用户 ID 查询数据库返回用户的详细信息（不含密码等敏感字段）。
     * </p>
     *
     * @param userId 用户 ID
     * @return 用户视图对象，包含用户名、邮箱、角色、状态等信息
     * @throws com.zjw.booknexus.exception.BusinessException 当用户不存在时抛出
     */
    UserVO getCurrentUser(Long userId);
}
