package com.zjw.booknexus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjw.booknexus.MvcTestConfig;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BorrowPageReq;
import com.zjw.booknexus.dto.BorrowReq;
import com.zjw.booknexus.service.BorrowService;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.BorrowRecordVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("借阅控制器集成测试")
@WebMvcTest(controllers = BorrowController.class)
@ContextConfiguration(classes = {BorrowController.class, JwtUtils.class, MvcTestConfig.class, com.zjw.booknexus.exception.GlobalExceptionHandler.class})
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowService borrowService;

    private String userToken;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userToken = new JwtUtils().generateAccessToken(USER_ID, "USER");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldReturn201_whenBorrowBookSuccess() throws Exception {
        BorrowReq req = new BorrowReq();
        req.setBookId(100L);

        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(10L);
        vo.setUserId(USER_ID);
        vo.setBookId(100L);
        vo.setStatus("BORROWED");

        when(borrowService.borrow(eq(USER_ID), any(BorrowReq.class))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/user/borrows")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));
    }

    @Test
    void shouldReturn400_whenBorrowWithoutBookId() throws Exception {
        BorrowReq req = new BorrowReq();
        // bookId is null

        mockMvc.perform(post("/api/v1/user/borrows")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn401_whenBorrowWithoutToken() throws Exception {
        BorrowReq req = new BorrowReq();
        req.setBookId(100L);

        mockMvc.perform(post("/api/v1/user/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200_whenReturnBookSuccess() throws Exception {
        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(10L);
        vo.setUserId(USER_ID);
        vo.setStatus("RETURNED");

        when(borrowService.returnBook(USER_ID, 10L)).thenReturn(vo);

        mockMvc.perform(put("/api/v1/user/borrows/10/return")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("RETURNED"));
    }

    @Test
    void shouldReturn200_whenRenewBookSuccess() throws Exception {
        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(10L);
        vo.setUserId(USER_ID);
        vo.setStatus("RENEWED");
        vo.setRenewCount(1);

        when(borrowService.renew(USER_ID, 10L)).thenReturn(vo);

        mockMvc.perform(put("/api/v1/user/borrows/10/renew")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("RENEWED"))
                .andExpect(jsonPath("$.data.renewCount").value(1));
    }

    @Test
    void shouldReturn200_whenQueryMyBorrows() throws Exception {
        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(10L);
        vo.setUserId(USER_ID);
        vo.setBookTitle("Test Book");
        vo.setStatus("BORROWED");

        when(borrowService.myBorrows(eq(USER_ID), any(BorrowPageReq.class)))
                .thenReturn(new PageResult<>(List.of(vo), 1L, 1L, 10L));

        mockMvc.perform(get("/api/v1/user/borrows")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].bookTitle").value("Test Book"));
    }

    @Test
    void shouldReturn401_whenQueryMyBorrowsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/borrows"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400_whenAdminAccessUserBorrowsWithInvalidBody() throws Exception {
        String adminToken = new JwtUtils().generateAccessToken(1L, "ADMIN");

        mockMvc.perform(post("/api/v1/user/borrows")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowReq())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
