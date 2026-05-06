package com.zjw.booknexus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjw.booknexus.MvcTestConfig;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookCreateReq;
import com.zjw.booknexus.dto.BookUpdateReq;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.BookVO;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("管理端图书控制器集成测试")
@WebMvcTest(controllers = AdminBookController.class)
@ContextConfiguration(classes = {AdminBookController.class, JwtUtils.class, MvcTestConfig.class, com.zjw.booknexus.exception.GlobalExceptionHandler.class})
class AdminBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = new JwtUtils().generateAccessToken(1L, "ADMIN");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldReturn200_whenAdminQueryBooks() throws Exception {
        BookVO book = new BookVO();
        book.setId(1L);
        book.setTitle("Admin Book");

        when(bookService.page(any())).thenReturn(
                new PageResult<>(List.of(book), 1L, 1L, 10L));

        mockMvc.perform(get("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("Admin Book"));
    }

    @Test
    void shouldReturn403_whenUserAccessAdminBooks() throws Exception {
        String userToken = new JwtUtils().generateAccessToken(2L, "USER");

        mockMvc.perform(get("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenAdminQueryWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn201_whenAdminCreateBook() throws Exception {
        BookCreateReq req = new BookCreateReq();
        req.setIsbn("978-1111111111");
        req.setTitle("New Book");
        req.setAuthor("Author");
        req.setPublisher("Publisher");
        req.setPublishedDate(LocalDate.of(2024, 1, 1));
        req.setBookshelfId(1L);
        req.setCategoryIds(List.of(1L));

        BookVO vo = new BookVO();
        vo.setId(1L);
        vo.setTitle("New Book");
        vo.setIsbn("978-1111111111");

        when(bookService.create(any(BookCreateReq.class))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.title").value("New Book"));
    }

    @Test
    void shouldReturn400_whenCreateBookWithInvalidData() throws Exception {
        BookCreateReq req = new BookCreateReq();
        // missing required fields like isbn, title, author

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn200_whenAdminUpdateBook() throws Exception {
        BookUpdateReq req = new BookUpdateReq();
        req.setTitle("Updated Title");

        BookVO vo = new BookVO();
        vo.setId(1L);
        vo.setTitle("Updated Title");

        when(bookService.update(eq(1L), any(BookUpdateReq.class))).thenReturn(vo);

        mockMvc.perform(put("/api/v1/admin/books/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void shouldReturn200_whenAdminDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/books/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
