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

/**
 * 管理端借阅管理控制器，提供借阅记录的查询和管理功能。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/borrows。
 * 包含借阅记录分页查询和后台强制归还操作。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/admin/borrows")
@RequiredArgsConstructor
public class AdminBorrowController {

    private final BorrowService borrowService;

    /**
     * 管理员分页查询借阅记录接口。
     * <p>
     * GET /api/v1/admin/borrows
     * 支持按借阅状态、用户 ID 及关键词（用户名/图书名）进行筛选，
     * 结果按创建时间倒序排列，方便管理员全面掌握借阅情况。
     * </p>
     *
     * @param req 分页查询参数，包含页码、每页大小、状态、用户 ID、关键词等筛选条件
     * @return 所有借阅记录的分页列表统一响应
     */
    @GetMapping
    public Result<PageResult<BorrowRecordVO>> adminPage(AdminBorrowPageReq req) {
        return Result.success(borrowService.adminPage(req));
    }

    /**
     * 管理员审批借阅申请接口。
     * <p>
     * PUT /api/v1/admin/borrows/{id}/approve
     * 管理员审批用户的借阅申请，通过后记录状态变为 BORROWED 并扣减库存。
     * </p>
     *
     * @param id 借阅记录 ID
     * @return 更新后的借阅记录详细信息统一响应
     */
    @PutMapping("/{id}/approve")
    public Result<BorrowRecordVO> approveBorrow(@PathVariable Long id) {
        return Result.success(borrowService.approveBorrow(id));
    }

    /**
     * 管理员确认归还图书接口。
     * <p>
     * PUT /api/v1/admin/borrows/{id}/confirm-return
     * 管理员确认用户归还的图书已入库，记录状态变为 RETURNED 并恢复库存。
     * </p>
     *
     * @param id 借阅记录 ID
     * @return 更新后的借阅记录详细信息统一响应
     */
    @PutMapping("/{id}/confirm-return")
    public Result<BorrowRecordVO> confirmReturn(@PathVariable Long id) {
        return Result.success(borrowService.confirmReturn(id));
    }

    /**
     * 管理员强制归还图书接口。
     * <p>
     * PUT /api/v1/admin/borrows/{id}/return
     * 管理员可强制将指定借阅记录的图书标记为已归还，
     * 适用于用户线下还书等场景。归还后自动计算逾期罚金。
     * </p>
     *
     * @param id 借阅记录 ID
     * @return 更新后的借阅记录详细信息统一响应
     */
    @PutMapping("/{id}/return")
    public Result<BorrowRecordVO> adminReturn(@PathVariable Long id) {
        return Result.success(borrowService.adminReturnRecord(id));
    }
}
