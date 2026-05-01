package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.BorrowPageReq;
import com.zjw.booknexus.dto.BorrowReq;
import com.zjw.booknexus.service.BorrowService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.BorrowRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/borrows")
    public Result<BorrowRecordVO> borrow(@Valid @RequestBody BorrowReq req) {
        return Result.created(borrowService.borrow(UserContext.getUserId(), req));
    }

    @PutMapping("/borrows/{id}/return")
    public Result<BorrowRecordVO> returnBook(@PathVariable Long id) {
        return Result.success(borrowService.returnBook(UserContext.getUserId(), id));
    }

    @PutMapping("/borrows/{id}/renew")
    public Result<BorrowRecordVO> renew(@PathVariable Long id) {
        return Result.success(borrowService.renew(UserContext.getUserId(), id));
    }

    @GetMapping("/borrows")
    public Result<PageResult<BorrowRecordVO>> myBorrows(@Valid BorrowPageReq req) {
        return Result.success(borrowService.myBorrows(UserContext.getUserId(), req));
    }
}
