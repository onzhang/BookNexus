package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/api/v1/public/books")
    public Result<PageResult<BookVO>> page(BookPageReq req) {
        return Result.success(bookService.page(req));
    }

    @GetMapping("/api/v1/public/books/{id}")
    public Result<BookVO> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }
}
