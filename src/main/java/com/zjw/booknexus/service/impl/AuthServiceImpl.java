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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

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

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    private LoginResp buildLoginResp(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getRole());

        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        return new LoginResp(accessToken, refreshToken, user.getId(), user.getUsername(), user.getRole());
    }

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
