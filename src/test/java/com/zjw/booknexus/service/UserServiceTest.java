package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.UserPageReq;
import com.zjw.booknexus.dto.UserUpdateReq;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.UserMapper;
import com.zjw.booknexus.service.impl.UserServiceImpl;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setRole("USER");
        testUser.setStatus("ENABLED");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void pageUsers_shouldReturnPageResult() {
        UserPageReq req = new UserPageReq();
        req.setPage(1);
        req.setSize(10);

        Page<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testUser));
        mockPage.setTotal(1);

        when(userMapper.selectPage(any(), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        PageResult<UserVO> result = userService.page(req);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("testuser", result.getRecords().get(0).getUsername());
    }

    @Test
    void pageUsers_shouldSearchByKeyword() {
        UserPageReq req = new UserPageReq();
        req.setKeyword("test");

        Page<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testUser));
        mockPage.setTotal(1);

        when(userMapper.selectPage(any(), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        PageResult<UserVO> result = userService.page(req);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(userMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getUserById_shouldReturnUserVO() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        UserVO vo = userService.getById(1L);

        assertNotNull(vo);
        assertEquals("testuser", vo.getUsername());
        assertEquals("USER", vo.getRole());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getById(99L));
        assertEquals(404, ex.getCode());
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void updateUser_shouldUpdateFields() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UserUpdateReq req = new UserUpdateReq();
        req.setEmail("new@example.com");
        req.setPhone("13900139000");

        userService.update(1L, req);

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUser_shouldThrowWhenNotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        UserUpdateReq req = new UserUpdateReq();
        req.setEmail("new@example.com");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.update(99L, req));
        assertEquals(404, ex.getCode());
    }

    @Test
    void updateUser_selfCannotDisable() {
        UserContext.set(1L, "ADMIN");
        when(userMapper.selectById(1L)).thenReturn(testUser);

        UserUpdateReq req = new UserUpdateReq();
        req.setStatus("DISABLED");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.update(1L, req));
        assertEquals(403, ex.getCode());
    }

    @Test
    void updateUserStatus_shouldUpdate() {
        when(userMapper.selectById(2L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateStatus(2L, "DISABLED");

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUserStatus_shouldThrowWhenNotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateStatus(99L, "ENABLED"));
        assertEquals(404, ex.getCode());
    }

    @Test
    void updateUserStatus_selfCannotDisable() {
        UserContext.set(1L, "ADMIN");
        testUser.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateStatus(1L, "DISABLED"));
        assertEquals(403, ex.getCode());
    }
}
