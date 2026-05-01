package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.AdminBorrowPageReq;
import com.zjw.booknexus.service.BorrowService;
import com.zjw.booknexus.vo.BorrowRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/borrows")
@RequiredArgsConstructor
public class AdminBorrowController {

    private final BorrowService borrowService;

    @GetMapping
    public Result<PageResult<BorrowRecordVO>> adminPage(AdminBorrowPageReq req) {
        return Result.success(borrowService.adminPage(req));
    }

    @PutMapping("/{id}/return")
    public Result<BorrowRecordVO> adminReturn(@PathVariable Long id) {
        return Result.success(borrowService.adminReturnRecord(id));
    }
}
