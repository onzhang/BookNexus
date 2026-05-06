package com.zjw.booknexus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.AnnouncementCreateReq;
import com.zjw.booknexus.dto.AnnouncementPageReq;
import com.zjw.booknexus.dto.AnnouncementUpdateReq;
import com.zjw.booknexus.entity.Announcement;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.AnnouncementMapper;
import com.zjw.booknexus.service.AnnouncementService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公告服务实现类，实现公告相关的完整业务逻辑。
 * <p>
 * 处理公告的分页搜索、详情查询、创建、更新和删除操作。
 * 使用 Hutool BeanUtil 实现属性拷贝，MyBatis-Plus 实现数据访问，
 * 事务注解确保数据一致性。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    /**
     * 分页查询公告。
     * <p>
     * 构建动态查询条件：关键词模糊匹配（标题），结果按创建时间倒序排列。
     * 查询结果流式转换为 AnnouncementVO。
     * </p>
     *
     * @param req 分页查询参数
     * @return 公告分页结果
     */
    @Override
    public PageResult<AnnouncementVO> page(AnnouncementPageReq req) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getKeyword())) {
            wrapper.like(Announcement::getTitle, req.getKeyword());
        }
        wrapper.orderByDesc(Announcement::getCreatedAt);

        Page<Announcement> mpPage = new Page<>(req.getPage(), req.getSize());
        Page<Announcement> result = announcementMapper.selectPage(mpPage, wrapper);

        List<AnnouncementVO> voList = result.getRecords().stream()
                .map(this::toAnnouncementVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 根据 ID 查询公告详情。
     *
     * @param id 公告 ID
     * @return 公告视图对象
     * @throws BusinessException 当公告不存在时抛出 404 异常
     */
    @Override
    public AnnouncementVO getById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        return toAnnouncementVO(announcement);
    }

    /**
     * 创建公告。
     * <p>
     * 使用 BeanUtil 拷贝属性创建公告实体，自动将当前登录用户设为发布人。
     * </p>
     *
     * @param req 公告创建请求
     * @return 新创建的公告视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO create(AnnouncementCreateReq req) {
        Announcement announcement = new Announcement();
        BeanUtil.copyProperties(req, announcement);
        announcement.setPublisherId(UserContext.getUserId());

        announcementMapper.insert(announcement);
        return toAnnouncementVO(announcement);
    }

    /**
     * 更新公告信息。
     * <p>
     * 支持部分字段更新。仅更新请求中携带的非空字段。
     * </p>
     *
     * @param id  公告 ID
     * @param req 公告更新请求
     * @return 更新后的公告视图对象
     * @throws BusinessException 当公告不存在时抛出 404 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO update(Long id, AnnouncementUpdateReq req) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(req.getTitle())) {
            announcement.setTitle(req.getTitle());
        }
        if (StrUtil.isNotBlank(req.getContent())) {
            announcement.setContent(req.getContent());
        }
        if (req.getIsPublished() != null) {
            announcement.setIsPublished(req.getIsPublished());
        }

        announcementMapper.updateById(announcement);
        return toAnnouncementVO(announcementMapper.selectById(id));
    }

    /**
     * 删除公告。
     * <p>
     * 逻辑删除指定 ID 的公告记录（MyBatis-Plus @TableLogic 自动处理）。
     * </p>
     *
     * @param id 要删除的公告 ID
     * @throws BusinessException 当公告不存在时抛出 404 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        announcementMapper.deleteById(id);
    }

    /**
     * 将公告实体转换为视图对象。
     *
     * @param announcement 公告实体
     * @return 公告视图对象
     */
    private AnnouncementVO toAnnouncementVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtil.copyProperties(announcement, vo);
        return vo;
    }
}
