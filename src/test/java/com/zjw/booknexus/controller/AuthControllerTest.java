package com.zjw.booknexus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjw.booknexus.MvcTestConfig;
import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RefreshReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.service.AuthService;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("认证控制器集成测试")
@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class, JwtUtils.class, MvcTestConfig.class, com.zjw.booknexus.exception.GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldReturn201_whenRegisterSuccess() throws Exception {
        RegisterReq req = new RegisterReq();
        req.setUsername("newuser");
        req.setPassword("123456");
        req.setEmail("new@example.com");

        when(authService.register(any(RegisterReq.class)))
                .thenReturn(new LoginResp("access-token", "refresh-token", 1L, "newuser", "USER"));

        mockMvc.perform(post("/api/v1/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void shouldReturn400_whenRegisterWithInvalidData() throws Exception {
        RegisterReq req = new RegisterReq();
        req.setUsername("ab"); // too short
        req.setPassword("123"); // too short

        mockMvc.perform(post("/api/v1/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn200_whenLoginSuccess() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("testuser");
        req.setPassword("123456");

        when(authService.login(any(LoginReq.class)))
                .thenReturn(new LoginResp("access-token", "refresh-token", 1L, "testuser", "USER"));

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void shouldReturn400_whenLoginWithBlankPassword() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("testuser");
        req.setPassword("");

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn200_whenRefreshSuccess() throws Exception {
        RefreshReq req = new RefreshReq();
        req.setRefreshToken("valid-refresh-token");

        when(authService.refresh(any(RefreshReq.class)))
                .thenReturn(new LoginResp("new-access", "new-refresh", 1L, "testuser", "USER"));

        mockMvc.perform(post("/api/v1/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"));
    }

    @Test
    void shouldReturn400_whenRefreshWithBlankToken() throws Exception {
        RefreshReq req = new RefreshReq();
        req.setRefreshToken("");

        mockMvc.perform(post("/api/v1/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn401_whenGetMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200_whenGetMeWithValidToken() throws Exception {
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("testuser");
        userVO.setRole("USER");

        when(authService.getCurrentUser(1L)).thenReturn(userVO);

        String token = new JwtUtils().generateAccessToken(1L, "USER");
        mockMvc.perform(get("/api/v1/user/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }
}
