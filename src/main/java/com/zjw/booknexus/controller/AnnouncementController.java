package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.AnnouncementPageReq;
import com.zjw.booknexus.service.AnnouncementService;
import com.zjw.booknexus.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开公告查询控制器，提供公告公开查询接口。
 * <p>
 * 处理公告分页搜索和公告详情查看功能，所有接口均为公开访问，
 * 无需身份认证即可使用。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/public/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 公告分页搜索接口。
     * <p>
     * GET /api/v1/public/announcements
     * 支持按关键词（标题）进行模糊搜索，结果按创建时间倒序排列。
     * 仅返回已发布的公告（is_published = 1）。
     * </p>
     *
     * @param req 分页查询参数
     * @return 分页后的公告列表统一响应
     */
    @GetMapping
    public Result<PageResult<AnnouncementVO>> page(AnnouncementPageReq req) {
        return Result.success(announcementService.page(req));
    }

    /**
     * 公告详情查询接口。
     * <p>
     * GET /api/v1/public/announcements/{id}
     * 根据公告 ID 查询公告详细信息。
     * </p>
     *
     * @param id 公告 ID
     * @return 公告详细信息统一响应
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}
