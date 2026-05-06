package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.SubscriptionReq;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.Subscription;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.SubscriptionMapper;
import com.zjw.booknexus.service.SubscriptionService;
import com.zjw.booknexus.vo.SubscriptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订阅服务实现类，实现用户图书订阅相关的完整业务逻辑。
 * <p>
 * 处理用户订阅图书归还通知、取消订阅及订阅列表查询。
 * 核心业务规则包括：同一用户不能重复订阅同一本书、取消订阅时更新状态为已取消。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final BookMapper bookMapper;

    /**
     * 订阅图书归还通知。
     * <p>
     * 执行完整的订阅前校验：图书是否存在、用户是否已活跃订阅该书。
     * 校验通过后创建订阅记录，初始状态为活跃（is_active = 1）。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    订阅请求，包含图书 ID
     * @return 订阅记录视图对象，包含图书名、作者等关联信息
     * @throws BusinessException 当图书不存在时抛出 404 异常，
     *         当已订阅该书时抛出 409 异常
     */
    @Override
    @Transactional
    public SubscriptionVO subscribe(Long userId, SubscriptionReq req) {
        // 1. 校验图书是否存在
        Book book = bookMapper.selectById(req.getBookId());
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }

        // 2. 校验用户是否已活跃订阅该书
        long existingCount = subscriptionMapper.selectCount(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getBookId, req.getBookId())
                .eq(Subscription::getIsActive, 1));
        if (existingCount > 0) {
            throw new BusinessException(409, ErrorCode.ALREADY_SUBSCRIBED);
        }

        // 3. 创建订阅记录（活跃状态）
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setBookId(req.getBookId());
        subscription.setIsActive(1);
        subscription.setCreatedAt(LocalDateTime.now());
        subscriptionMapper.insert(subscription);

        // 4. 组装视图对象返回
        return buildVO(subscription, book);
    }

    /**
     * 取消订阅。
     * <p>
     * 根据当前用户 ID 和图书 ID 查询活跃订阅记录，
     * 将订阅状态更新为已取消（is_active = 0）并记录更新时间。
     * 采用软删除方式保留订阅历史记录。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @throws BusinessException 当活跃订阅记录不存在时抛出 404 异常
     */
    @Override
    @Transactional
    public void unsubscribe(Long userId, Long bookId) {
        // 按用户 ID 和图书 ID 查询活跃订阅记录
        Subscription subscription = subscriptionMapper.selectOne(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getBookId, bookId)
                .eq(Subscription::getIsActive, 1));
        if (subscription == null) {
            throw new BusinessException(404, ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        // 更新为已取消状态，记录更新时间
        subscription.setIsActive(0);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionMapper.updateById(subscription);
    }

    /**
     * 查询当前用户的活跃订阅列表。
     * <p>
     * 分页查询指定用户的活跃订阅记录（is_active = 1），按订阅时间倒序排列。
     * 每条记录查询对应的图书信息并组装为视图对象。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param page   当前页码
     * @param size   每页大小
     * @return 订阅记录分页结果
     */
    @Override
    public PageResult<SubscriptionVO> mySubscriptions(Long userId, Integer page, Integer size) {
        // 1. 构建查询条件：按当前用户 ID 精确过滤，仅查询活跃订阅，按创建时间倒序
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getIsActive, 1)
                .orderByDesc(Subscription::getCreatedAt);

        // 2. 执行分页查询
        Page<Subscription> pageParam = new Page<>(page, size);
        IPage<Subscription> result = subscriptionMapper.selectPage(pageParam, wrapper);

        // 3. 流式转换为 VO：每条记录查询对应的图书信息
        List<SubscriptionVO> vos = result.getRecords().stream()
                .map(subscription -> {
                    Book book = bookMapper.selectById(subscription.getBookId());
                    return buildVO(subscription, book);
                }).toList();

        // 4. 组装分页结果返回
        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 检查用户是否已活跃订阅指定图书。
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @return true=已订阅（活跃状态），false=未订阅
     */
    @Override
    public boolean isSubscribed(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        return subscriptionMapper.selectCount(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getBookId, bookId)
                .eq(Subscription::getIsActive, 1)) > 0;
    }

    /**
     * 构建订阅记录视图对象。
     * <p>
     * 将订阅记录实体与关联的图书信息组装为视图对象。
     * 图书可能为空（如已被删除），此时对应的名称字段置为 null。
     * </p>
     *
     * @param subscription 订阅记录实体
     * @param book         关联的图书实体（可能为 null）
     * @return 订阅记录视图对象
     */
    private SubscriptionVO buildVO(Subscription subscription, Book book) {
        SubscriptionVO vo = new SubscriptionVO();
        vo.setId(subscription.getId());
        vo.setUserId(subscription.getUserId());
        vo.setBookId(subscription.getBookId());
        vo.setBookTitle(book != null ? book.getTitle() : null);
        vo.setBookAuthor(book != null ? book.getAuthor() : null);
        vo.setBookCoverUrl(book != null ? book.getCoverUrl() : null);
        vo.setIsActive(subscription.getIsActive());
        vo.setCreatedAt(subscription.getCreatedAt());
        vo.setUpdatedAt(subscription.getUpdatedAt());
        return vo;
    }
}
