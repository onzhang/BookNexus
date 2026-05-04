package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.UserMapper;
import com.zjw.booknexus.service.impl.AuthServiceImpl;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        testUser.setStatus("ENABLED");
        testUser.setCreatedAt(LocalDateTime.now());

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterReq req = new RegisterReq();
        req.setUsername("newuser");
        req.setPassword("123456");
        req.setEmail("new@example.com");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));
        when(jwtUtils.generateAccessToken(anyLong(), anyString())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh-token");

        LoginResp resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("access-token", resp.getAccessToken());
        assertEquals("refresh-token", resp.getRefreshToken());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterReq req = new RegisterReq();
        req.setUsername("testuser");
        req.setPassword("123456");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertEquals(409, ex.getCode());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsValid() {
        LoginReq req = new LoginReq();
        req.setUsername("testuser");
        req.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("123456", testUser.getPassword())).thenReturn(true);
        when(jwtUtils.generateAccessToken(1L, "USER")).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(1L, "USER")).thenReturn("refresh-token");

        LoginResp resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("access-token", resp.getAccessToken());
        assertEquals(1L, resp.getUserId());
        assertEquals("USER", resp.getRole());
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginReq req = new LoginReq();
        req.setUsername("unknown");
        req.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(req));
        assertEquals(401, ex.getCode());
    }

    @Test
    void login_shouldThrowWhenPasswordWrong() {
        LoginReq req = new LoginReq();
        req.setUsername("testuser");
        req.setPassword("wrong");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("wrong", testUser.getPassword())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(req));
        assertEquals(401, ex.getCode());
    }

    @Test
    void getCurrentUser_shouldReturnUserVO() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        UserVO vo = authService.getCurrentUser(1L);

        assertNotNull(vo);
        assertEquals("testuser", vo.getUsername());
        assertEquals("USER", vo.getRole());
    }

    @Test
    void getCurrentUser_shouldThrowWhenNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.getCurrentUser(1L));
        assertEquals(404, ex.getCode());
    }
}
