package com.zjw.booknexus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.NotificationPageReq;
import com.zjw.booknexus.entity.Notification;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.NotificationMapper;
import com.zjw.booknexus.service.NotificationService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务实现类，实现通知相关的业务逻辑。
 * <p>
 * 处理当前用户通知的分页查询和标记已读操作。
 * 使用 Hutool BeanUtil 实现属性拷贝，MyBatis-Plus 实现数据访问。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 分页查询当前用户的通知列表。
     * <p>
     * 构建动态查询条件：按用户 ID 精确匹配，支持按已读状态筛选。
     * 结果按创建时间倒序排列，最新的通知优先展示。
     * </p>
     *
     * @param req 分页查询参数
     * @return 通知分页结果
     */
    @Override
    public PageResult<NotificationVO> page(NotificationPageReq req) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        // 按当前用户筛选
        wrapper.eq(Notification::getUserId, UserContext.getUserId());
        if (req.getIsRead() != null) {
            wrapper.eq(Notification::getIsRead, req.getIsRead());
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> mpPage = new Page<>(req.getPage(), req.getSize());
        Page<Notification> result = notificationMapper.selectPage(mpPage, wrapper);

        List<NotificationVO> voList = result.getRecords().stream()
                .map(this::toNotificationVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 标记通知为已读。
     * <p>
     * 校验通知是否存在且属于当前用户，然后将 is_read 设为 1。
     * </p>
     *
     * @param id     通知 ID
     * @param userId 当前用户 ID
     * @throws BusinessException 当通知不存在或不属于当前用户时抛出 404 异常
     */
    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(404, ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, ErrorCode.FORBIDDEN);
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    /**
     * 将通知实体转换为视图对象。
     *
     * @param notification 通知实体
     * @return 通知视图对象
     */
    private NotificationVO toNotificationVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtil.copyProperties(notification, vo);
        return vo;
    }
}
