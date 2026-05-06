package com.zjw.booknexus.controller;

import com.zjw.booknexus.MvcTestConfig;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.utils.JwtUtils;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.BookVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("图书查询控制器集成测试")
@WebMvcTest(controllers = BookController.class)
@ContextConfiguration(classes = {BookController.class, JwtUtils.class, MvcTestConfig.class, com.zjw.booknexus.exception.GlobalExceptionHandler.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldReturn200_whenQueryPublicBooks() throws Exception {
        BookVO book = new BookVO();
        book.setId(1L);
        book.setTitle("Spring in Action");
        book.setAuthor("Craig Walls");

        PageResult<BookVO> pageResult = new PageResult<>(
                List.of(book), 1L, 1L, 10L
        );

        when(bookService.page(any(BookPageReq.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/public/books")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Spring in Action"));
    }

    @Test
    void shouldReturn200_whenGetBookById() throws Exception {
        BookVO book = new BookVO();
        book.setId(1L);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("978-0132350884");

        when(bookService.getById(1L)).thenReturn(book);

        mockMvc.perform(get("/api/v1/public/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Clean Code"))
                .andExpect(jsonPath("$.data.isbn").value("978-0132350884"));
    }

    @Test
    void shouldReturn200_whenQueryBooksWithoutParams() throws Exception {
        when(bookService.page(any(BookPageReq.class)))
                .thenReturn(new PageResult<>(List.of(), 0L, 1L, 10L));

        mockMvc.perform(get("/api/v1/public/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
