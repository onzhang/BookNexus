package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.MessageReplyReq;
import com.zjw.booknexus.service.MessageService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端留言管理控制器，提供查看所有留言和回复留言功能。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/messages。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;

    /**
     * 查看所有留言接口。
     * <p>
     * GET /api/v1/admin/messages
     * 分页查询所有用户的留言列表，结果按创建时间倒序排列。
     * </p>
     *
     * @param page 当前页码
     * @param size 每页大小
     * @return 留言分页结果统一响应
     */
    @GetMapping
    public Result<PageResult<MessageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(messageService.pageAll(page, size));
    }

    /**
     * 回复留言接口。
     * <p>
     * PUT /api/v1/admin/messages/{id}/reply
     * 管理员对指定留言进行回复，自动记录回复时间和回复人。
     * </p>
     *
     * @param id  留言 ID
     * @param req 回复请求体
     * @return 更新后的留言详细信息统一响应
     */
    @PutMapping("/{id}/reply")
    public Result<MessageVO> reply(@PathVariable Long id, @Valid @RequestBody MessageReplyReq req) {
        return Result.success(messageService.reply(id, req, UserContext.getUserId()));
    }
}
