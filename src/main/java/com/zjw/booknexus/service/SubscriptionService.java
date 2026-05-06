package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.SubscriptionReq;
import com.zjw.booknexus.vo.SubscriptionVO;

/**
 * 订阅服务接口，定义用户图书订阅相关的核心业务逻辑。
 * <p>
 * 包含用户端的订阅图书归还通知、取消订阅及订阅列表查询功能。
 * 当图书可借时系统会自动向订阅用户推送通知。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
public interface SubscriptionService {

    /**
     * 订阅图书归还通知。
     * <p>
     * 校验图书是否存在，且用户尚未订阅该书。
     * 通过校验后创建订阅记录。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    订阅请求，包含图书 ID
     * @return 订阅记录视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当图书不存在时抛出 404 异常，
     *         当已订阅该书时抛出 409 异常
     */
    SubscriptionVO subscribe(Long userId, SubscriptionReq req);

    /**
     * 取消订阅。
     * <p>
     * 根据当前用户 ID 和图书 ID 取消对应的订阅记录。
     * 仅将订阅状态更新为已取消（软取消），记录保留以便追溯。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当订阅记录不存在时抛出 404 异常
     */
    void unsubscribe(Long userId, Long bookId);

    /**
     * 查询当前用户的订阅列表。
     * <p>
     * 分页查询指定用户的活跃订阅记录（is_active = 1），
     * 结果按订阅时间倒序排列。每条记录附带对应的图书信息。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param page   当前页码
     * @param size   每页大小
     * @return 订阅记录分页结果
     */
    PageResult<SubscriptionVO> mySubscriptions(Long userId, Integer page, Integer size);

    /**
     * 检查用户是否已订阅指定图书。
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @return true=已订阅（活跃状态），false=未订阅
     */
    boolean isSubscribed(Long userId, Long bookId);
}
