package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RefreshReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.UserMapper;
import com.zjw.booknexus.service.AuthService;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类，实现用户认证相关的核心业务逻辑。
 * <p>
 * 处理用户注册时的唯一性校验和密码 BCrypt 加密、登录时的身份认证和账户状态检查、
 * 基于 JWT 的访问令牌和刷新令牌的生成与管理、以及令牌刷新和当前用户信息查询。
 * 刷新令牌存储在 Redis 中，支持主动失效，有效期为 7 天。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 用户注册。
     * <p>
     * 依次校验用户名和邮箱的唯一性，校验通过后创建新用户，
     * 使用 BCrypt 对密码进行加密，默认分配 USER 角色和 ENABLED 状态，
     * 注册成功后自动生成访问令牌和刷新令牌并持久化刷新令牌至 Redis。
     * </p>
     *
     * @param req 注册请求，包含用户名、密码、邮箱等信息
     * @return 登录响应，包含访问令牌、刷新令牌及用户基本信息
     * @throws BusinessException 当用户名或邮箱已存在时抛出 409 异常
     */
    @Override
    @Transactional
    public LoginResp register(RegisterReq req) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())) > 0) {
            throw new BusinessException(409, ErrorCode.USERNAME_EXISTS);
        }
        if (req.getEmail() != null && !req.getEmail().isEmpty()
                && userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())) > 0) {
            throw new BusinessException(409, ErrorCode.EMAIL_EXISTS);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole("USER");
        user.setStatus("ENABLED");
        userMapper.insert(user);

        return buildLoginResp(user);
    }

    /**
     * 用户登录认证。
     * <p>
     * 根据用户名查询用户信息，依次校验：用户是否存在、账户是否被禁用、
     * 密码是否匹配。全部校验通过后生成访问令牌和刷新令牌，
     * 并将刷新令牌存储至 Redis（有效期 7 天）。
     * </p>
     *
     * @param req 登录请求，包含用户名和密码
     * @return 登录响应，包含访问令牌、刷新令牌及用户基本信息
     * @throws BusinessException 当用户名不存在或密码错误时抛出 401 异常，
     *         当账户已被禁用时抛出 403 异常
     */
    @Override
    public LoginResp login(LoginReq req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(401, ErrorCode.INVALID_CREDENTIALS);
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, ErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, ErrorCode.INVALID_CREDENTIALS);
        }

        return buildLoginResp(user);
    }

    /**
     * 刷新访问令牌。
     * <p>
     * 校验刷新令牌的 JWT 签名和类型（type=refresh），从令牌中解析用户 ID 和角色，
     * 查询用户是否仍有效（未删除、未禁用），校验通过后生成新的令牌对。
     * 新生成的刷新令牌将更新 Redis 中的存储。
     * </p>
     *
     * @param req 刷新请求，包含当前有效的刷新令牌
     * @return 登录响应，包含新生成的访问令牌和刷新令牌
     * @throws BusinessException 当刷新令牌无效、已过期、类型不匹配或用户已失效时抛出 401 异常
     */
    @Override
    public LoginResp refresh(RefreshReq req) {
        Claims claims;
        try {
            claims = jwtUtils.parseToken(req.getRefreshToken());
        } catch (Exception e) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        User user = userMapper.selectById(userId);
        if (user == null || "DISABLED".equals(user.getStatus())) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        return buildLoginResp(user);
    }

    /**
     * 获取当前登录用户信息。
     * <p>
     * 根据用户 ID 从数据库查询用户，将用户实体转换为不包含敏感字段的视图对象返回。
     * </p>
     *
     * @param userId 用户 ID
     * @return 用户视图对象，包含用户名、邮箱、电话、角色、状态、头像和创建时间
     * @throws BusinessException 当用户不存在时抛出 404 异常
     */
    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    /**
     * 构建登录响应。
     * <p>
     * 为用户生成访问令牌（Access Token）和刷新令牌（Refresh Token），
     * 将刷新令牌存储到 Redis（键格式：refresh:{userId}，有效期 7 天），
     * 并组装登录响应对象。
     * </p>
     *
     * @param user 用户实体
     * @return 包含令牌和用户基本信息的登录响应
     */
    private LoginResp buildLoginResp(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getRole());

        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        return new LoginResp(accessToken, refreshToken, user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * 将用户实体转换为视图对象。
     * <p>
     * 拷贝用户信息到视图对象，排除密码等敏感字段，
     * 并将 LocalDateTime 类型的创建时间格式化为字符串。
     * </p>
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return vo;
    }
}
