package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RefreshReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.entity.Notification;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.enums.NotificationType;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.NotificationMapper;
import com.zjw.booknexus.mapper.UserMapper;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zjw.booknexus.sentinel.SentinelRuleInitializer;
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
    private final NotificationMapper notificationMapper;
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
    @SentinelResource(value = "register", fallback = "fallbackLoginResp", fallbackClass = SentinelRuleInitializer.class)
    @Transactional(rollbackFor = Exception.class)
    public LoginResp register(RegisterReq req) {
        // 1. 校验用户名唯一性：若已存在则拒绝注册，防止重复用户名
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())) > 0) {
            throw new BusinessException(409, ErrorCode.USERNAME_EXISTS);
        }
        // 2. 校验邮箱唯一性：邮箱为选填项，空字符串视为未填写
        String email = (req.getEmail() != null && !req.getEmail().isBlank()) ? req.getEmail() : null;
        if (email != null
                && userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0) {
            throw new BusinessException(409, ErrorCode.EMAIL_EXISTS);
        }

        // 3. 创建新用户：密码经 BCrypt 加密后存储，默认角色为普通用户，账户默认启用
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus("ENABLED");
        userMapper.insert(user);

        // 4. 为新注册用户创建欢迎通知
        createWelcomeNotifications(user.getId());

        // 5. 注册成功后自动生成令牌对并返回登录响应
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
    @SentinelResource(value = "login", fallback = "fallbackLoginResp", fallbackClass = SentinelRuleInitializer.class)
    public LoginResp login(LoginReq req) {
        // 1. 根据用户名查询用户是否存在；不存在则返回 401 避免泄露用户是否注册
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(401, ErrorCode.INVALID_CREDENTIALS);
        }
        // 2. 检查账户状态：已被禁用的账户拒绝登录
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, ErrorCode.ACCOUNT_DISABLED);
        }
        // 3. 校验密码：使用 BCrypt 比较明文和密文是否匹配
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, ErrorCode.INVALID_CREDENTIALS);
        }

        // 4. 为新用户（尚无任何通知记录）补发欢迎通知，兼容注册功能上线前的存量用户
        long notificationCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, user.getId()));
        if (notificationCount == 0) {
            createWelcomeNotifications(user.getId());
        }

        // 5. 校验通过，生成令牌对并返回登录响应
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
    @SentinelResource(value = "refresh", fallback = "fallbackLoginResp", fallbackClass = SentinelRuleInitializer.class)
    public LoginResp refresh(RefreshReq req) {
        // 1. 解析 Refresh Token：签名或过期解析失败则判定令牌无效
        Claims claims;
        try {
            claims = jwtUtils.parseToken(req.getRefreshToken());
        } catch (Exception e) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        // 2. 校验令牌类型：仅接受 type=refresh 的令牌，防止用 Access Token 冒充
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        // 3. 从令牌中提取用户身份信息
        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }
        String role = claims.get("role", String.class);

        // 4. 校验刷新令牌是否仍存在于 Redis（防止已吊销的令牌被重放）
        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(req.getRefreshToken())) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        // 5. 查询用户当前状态：用户已被删除或禁用时拒绝颁发新令牌
        User user = userMapper.selectById(userId);
        if (user == null || "DISABLED".equals(user.getStatus())) {
            throw new BusinessException(401, ErrorCode.TOKEN_INVALID);
        }

        // 6. 校验通过，生成新的令牌对（旧 Refresh Token 将因 Redis 覆盖而失效）
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
        // 1. 根据用户 ID 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }
        // 2. 转换为不含敏感字段的视图对象返回
        return toVO(user);
    }

    /**
     * 用户登出，删除 Redis 中的 Refresh Token。
     *
     * @param userId 当前用户 ID
     */
    @Override
    public void logout(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    /**
     * 为新注册（或首次登录的存量）用户创建欢迎通知。
     * <p>
     * 自动插入一条欢迎消息和一条系统使用须知，帮助新用户快速了解系统。
     * 通知类型为 {@link NotificationType#SYSTEM}，初始状态为未读。
     * </p>
     *
     * @param userId 用户 ID
     */
    private void createWelcomeNotifications(Long userId) {
        // 欢迎通知
        Notification welcome = new Notification();
        welcome.setUserId(userId);
        welcome.setType(NotificationType.SYSTEM.name());
        welcome.setTitle("欢迎加入 BookNexus！");
        welcome.setContent("尊敬的读者，欢迎您加入 BookNexus 图书管理系统！在这里您可以浏览丰富的图书资源、在线借阅、管理个人书架，享受便捷的阅读体验。如有任何疑问，请查看「系统使用须知」通知或联系管理员。");
        welcome.setIsRead(0);
        notificationMapper.insert(welcome);

        // 使用须知
        Notification guide = new Notification();
        guide.setUserId(userId);
        guide.setType(NotificationType.SYSTEM.name());
        guide.setTitle("系统使用须知");
        guide.setContent("【借阅规则】每位用户最多同时借阅 5 本图书，每本书默认借期 30 天，可续借 1 次（延长 15 天）。逾期将按 0.10 元/天收取罚金。\n\n"
                + "【图书搜索】支持按书名、作者、ISBN 搜索图书，也可使用全文搜索功能快速定位所需书籍。\n\n"
                + "【个人中心】在个人中心可查看借阅记录、收藏图书、管理订阅，以及接收系统通知。\n\n"
                + "【消息留言】如有建议或问题，欢迎通过留言功能与管理员沟通。");
        guide.setIsRead(0);
        notificationMapper.insert(guide);
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
        // 1. 生成 Access Token（30分钟有效期）和 Refresh Token（7天有效期）
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getRole());

        // 2. 将 Refresh Token 持久化至 Redis：键格式 "refresh:{userId}"，用于服务端吊销校验
        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        // 3. 组装登录响应，返回令牌对和用户基本信息
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
        // 将用户实体转换为视图对象：排除密码等敏感字段，仅暴露安全信息
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
