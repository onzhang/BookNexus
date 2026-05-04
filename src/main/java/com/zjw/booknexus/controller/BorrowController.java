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

/**
 * 用户端借阅控制器，提供个人借阅管理功能。
 * <p>
 * 处理当前登录用户的图书借阅、归还、续借及借阅记录查询操作。
 * 所有接口均需携带有效的访问令牌进行身份认证。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * 借阅图书接口。
     * <p>
     * POST /api/v1/user/borrows
     * 当前用户借阅指定图书，需满足：图书状态为 AVAILABLE、用户未借阅该书、
     * 用户当前借阅数量未超过上限（5本）等条件。
     * 借阅成功后图书状态更新为 BORROWED。
     * </p>
     *
     * @param req 借阅请求体，包含图书 ID
     * @return 借阅记录详细信息统一响应
     */
    @PostMapping("/borrows")
    public Result<BorrowRecordVO> borrow(@Valid @RequestBody BorrowReq req) {
        return Result.created(borrowService.borrow(UserContext.getUserId(), req));
    }

    /**
     * 归还图书接口。
     * <p>
     * PUT /api/v1/user/borrows/{id}/return
     * 当前用户归还指定借阅记录的图书。若归还日期超过应还日期，
     * 系统自动按逾期天数计算罚金（0.10元/天）。
     * 归还成功后图书状态恢复为 AVAILABLE。
     * </p>
     *
     * @param id 借阅记录 ID
     * @return 更新后的借阅记录详细信息统一响应
     */
    @PutMapping("/borrows/{id}/return")
    public Result<BorrowRecordVO> returnBook(@PathVariable Long id) {
        return Result.success(borrowService.returnBook(UserContext.getUserId(), id));
    }

    /**
     * 续借图书接口。
     * <p>
     * PUT /api/v1/user/borrows/{id}/renew
     * 当前用户对指定借阅记录进行续借操作，续借后应还日期延长 15 天。
     * 每本书最多续借 1 次，续借次数达到上限后不可再续借。
     * </p>
     *
     * @param id 借阅记录 ID
     * @return 续借后的借阅记录详细信息统一响应
     */
    @PutMapping("/borrows/{id}/renew")
    public Result<BorrowRecordVO> renew(@PathVariable Long id) {
        return Result.success(borrowService.renew(UserContext.getUserId(), id));
    }

    /**
     * 查询我的借阅记录接口。
     * <p>
     * GET /api/v1/user/borrows
     * 分页查询当前登录用户的借阅记录，可按借阅状态（BORROWED/RENEWED/RETURNED）进行筛选，
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数，包含页码、每页大小、借阅状态等筛选条件
     * @return 当前用户的借阅记录分页列表统一响应
     */
    @GetMapping("/borrows")
    public Result<PageResult<BorrowRecordVO>> myBorrows(@Valid BorrowPageReq req) {
        return Result.success(borrowService.myBorrows(UserContext.getUserId(), req));
    }
}
