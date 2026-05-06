package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.NotificationPageReq;
import com.zjw.booknexus.vo.NotificationVO;

/**
 * 通知服务接口，定义通知相关业务逻辑。
 * <p>
 * 包含当前用户通知的分页查询、标记已读等功能。
 * 通知由系统或 MQ 异步产生，管理端不提供管理接口。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface NotificationService {

    /**
     * 分页查询当前用户的通知列表。
     * <p>
     * 支持按已读状态筛选，结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 通知分页结果
     */
    PageResult<NotificationVO> page(NotificationPageReq req);

    /**
     * 标记通知为已读。
     *
     * @param id     通知 ID
     * @param userId 当前用户 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当通知不存在或不属于当前用户时抛出
     */
    void markAsRead(Long id, Long userId);
}
