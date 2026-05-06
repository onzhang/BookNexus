package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.AnnouncementCreateReq;
import com.zjw.booknexus.dto.AnnouncementPageReq;
import com.zjw.booknexus.dto.AnnouncementUpdateReq;
import com.zjw.booknexus.service.AnnouncementService;
import com.zjw.booknexus.vo.AnnouncementVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端公告管理控制器，提供公告的增删改查操作。
 * <p>
 * 仅管理员可访问，前缀为 /api/v1/admin/announcements。
 * 包含公告创建、信息更新、逻辑删除和分页查询功能。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@RestController
@RequestMapping("/api/v1/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 管理员分页查询公告接口。
     * <p>
     * GET /api/v1/admin/announcements
     * 支持按关键词（标题）进行模糊搜索，结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 公告分页结果统一响应
     */
    @GetMapping
    public Result<PageResult<AnnouncementVO>> page(AnnouncementPageReq req) {
        return Result.success(announcementService.page(req));
    }

    /**
     * 创建公告接口。
     * <p>
     * POST /api/v1/admin/announcements
     * 管理员新增公告，需提供标题、正文和发布状态。
     * 系统自动将当前登录用户设为发布人。
     * </p>
     *
     * @param req 公告创建请求体
     * @return 新创建的公告详细信息统一响应
     */
    @PostMapping
    public Result<AnnouncementVO> create(@Valid @RequestBody AnnouncementCreateReq req) {
        return Result.created(announcementService.create(req));
    }

    /**
     * 更新公告信息接口。
     * <p>
     * PUT /api/v1/admin/announcements/{id}
     * 管理员修改指定公告的信息，支持部分字段更新（标题、正文、发布状态）。
     * </p>
     *
     * @param id  公告 ID
     * @param req 公告更新请求体
     * @return 更新后的公告详细信息统一响应
     */
    @PutMapping("/{id}")
    public Result<AnnouncementVO> update(@PathVariable Long id, @Valid @RequestBody AnnouncementUpdateReq req) {
        return Result.success(announcementService.update(id, req));
    }

    /**
     * 删除公告接口。
     * <p>
     * DELETE /api/v1/admin/announcements/{id}
     * 管理员删除指定公告。逻辑删除，记录仍保留在数据库中。
     * </p>
     *
     * @param id 要删除的公告 ID
     * @return 统一成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
