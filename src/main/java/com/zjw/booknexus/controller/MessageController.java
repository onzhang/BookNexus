package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.MessageCreateReq;
import com.zjw.booknexus.dto.MessageReplyReq;
import com.zjw.booknexus.service.MessageService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端留言控制器，提供用户提交留言和查询我的留言列表功能。
 * <p>
 * 前缀为 /api/v1/user/messages，需登录后访问。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/user/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 我的留言列表接口。
     * <p>
     * GET /api/v1/user/messages
     * 分页查询当前登录用户的留言列表，结果按创建时间倒序排列。
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
        return Result.success(messageService.pageByUser(page, size, UserContext.getUserId()));
    }

    /**
     * 提交留言接口。
     * <p>
     * POST /api/v1/user/messages
     * 用户提交留言/建议，自动将当前登录用户设为留言人。
     * </p>
     *
     * @param req 留言创建请求体
     * @return 新创建的留言详细信息统一响应
     */
    @PostMapping
    public Result<MessageVO> create(@Valid @RequestBody MessageCreateReq req) {
        return Result.created(messageService.create(req, UserContext.getUserId()));
    }
}
