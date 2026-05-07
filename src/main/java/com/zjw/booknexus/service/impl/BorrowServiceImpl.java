package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.AdminBorrowPageReq;
import com.zjw.booknexus.dto.BorrowPageReq;
import com.zjw.booknexus.dto.BorrowReq;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.BorrowRecord;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.enums.BookStatus;
import com.zjw.booknexus.enums.BorrowStatus;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.BorrowRecordMapper;
import com.zjw.booknexus.mapper.UserMapper;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zjw.booknexus.sentinel.SentinelRuleInitializer;
import com.zjw.booknexus.service.BorrowService;
import com.zjw.booknexus.vo.BorrowRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

/**
 * 借阅服务实现类，实现图书借阅相关的完整业务逻辑。
 * <p>
 * 处理用户借书、还书、续借及借阅记录查询，以及管理端的借阅记录管理和强制归还。
 * 核心业务规则包括：每人最多同时借阅 5 本、每本书默认借期 30 天、
 * 每本书最多续借 1 次（延长 15 天）、逾期罚金 0.10 元/天。
 * 借阅操作涉及借阅记录、图书状态、用户信息的多表协同操作，
 * 关键业务方法均通过 @Transactional 保证事务一致性。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    private static final int MAX_ACTIVE_BORROWS = 5;
    private static final int BORROW_DAYS = 30;
    private static final int RENEW_DAYS = 15;
    private static final int MAX_RENEW_COUNT = 1;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.10");

    /**
     * 用户借阅图书。
     * <p>
     * 执行完整的借阅前校验：图书是否存在、图书状态是否可借阅、
     * 用户当前借阅中的数量是否未超上限（5 本）、用户是否未借阅该书。
     * 校验通过后创建借阅记录（借阅日期为当天，应还日期为 30 天后），
     * 并将图书状态更新为 BORROWED。整个操作在一个事务中完成。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    借阅请求，包含图书 ID
     * @return 借阅记录视图对象，包含图书名、用户名等关联信息
     * @throws BusinessException 当图书不存在时抛出 404 异常，
     *         当图书不可借阅、借阅数量超限或已借阅该书时抛出 409 异常
     */
    @Override
    @SentinelResource(value = "borrow", fallback = "fallbackObject", fallbackClass = SentinelRuleInitializer.class)
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecordVO borrow(Long userId, BorrowReq req) {
        // 1. 校验图书是否存在，不存在则直接拒绝借阅
        Book book = bookMapper.selectById(req.getBookId());
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        // 2. 校验图书是否仍有可用库存（可用库存大于 0 方可借出）
        if (book.getAvailableStock() <= 0) {
            throw new BusinessException(409, ErrorCode.BOOK_NOT_AVAILABLE);
        }

        // 3. 校验用户当前借阅数是否已达上限（最多同时借阅 5 本，含已续借的记录）
        long activeCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .in(BorrowRecord::getStatus, BorrowStatus.BORROWED.name(), BorrowStatus.RENEWED.name()));
        if (activeCount >= MAX_ACTIVE_BORROWS) {
            throw new BusinessException(409, ErrorCode.BORROW_LIMIT_EXCEEDED);
        }

        // 4. 校验用户是否已借阅该书且尚未归还（防止对同一本书重复借阅）
        long existingBorrow = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, req.getBookId())
                .in(BorrowRecord::getStatus, BorrowStatus.BORROWED.name(), BorrowStatus.RENEWED.name()));
        if (existingBorrow > 0) {
            throw new BusinessException(409, ErrorCode.ALREADY_BORROWED);
        }

        // 5. 创建借阅记录：借阅日期为当天，应还日期为 30 天后，初始罚金为 0
        LocalDate today = LocalDate.now();
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(req.getBookId());
        record.setBorrowDate(today);
        record.setDueDate(today.plusDays(BORROW_DAYS));
        record.setStatus(BorrowStatus.BORROWED.name());
        record.setRenewCount(0);
        record.setFineAmount(BigDecimal.ZERO);
        borrowRecordMapper.insert(record);

        // 6. 原子化扣减可用库存，防止并发超借
        int rows = bookMapper.decrementAvailableStock(req.getBookId());
        if (rows == 0) {
            throw new BusinessException(409, ErrorCode.BOOK_NOT_AVAILABLE);
        }
        // 扣减成功后重新查询图书状态，若库存降为 0 则更新状态为 BORROWED
        book = bookMapper.selectById(req.getBookId());
        if (book.getAvailableStock() == 0) {
            book.setStatus(BookStatus.BORROWED.name());
            bookMapper.updateById(book);
        }

        // 7. 查询用户信息并组装视图对象返回
        User user = userMapper.selectById(userId);
        return buildVO(record, book, user);
    }

    /**
     * 用户归还图书。
     * <p>
     * 根据当前用户 ID 和记录 ID 查询借阅记录，确保只有记录归属人可以执行归还。
     * 校验记录状态为 BORROWED 或 RENEWED 后方可执行归还操作。
     * 若归还日期超过应还日期，按 0.10 元/天计算逾期罚金。
     * 归还后更新借阅记录状态为 RETURNED 并将图书状态恢复为 AVAILABLE。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param recordId 借阅记录 ID
     * @return 更新后的借阅记录视图对象
     * @throws BusinessException 当记录不存在时抛出 404 异常，
     *         当记录状态不合法时抛出 409 异常
     */
    @Override
    @SentinelResource(value = "returnBook", fallback = "fallbackObject", fallbackClass = SentinelRuleInitializer.class)
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecordVO returnBook(Long userId, Long recordId) {
        // 1. 查询借阅记录：同时按记录 ID 和用户 ID 查询，确保只有记录归属人本人可以归还
        BorrowRecord record = borrowRecordMapper.selectOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getId, recordId)
                .eq(BorrowRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, ErrorCode.RECORD_NOT_FOUND);
        }
        // 2. 调用核心归还逻辑（状态校验、罚金计算、图书状态更新）
        return doReturn(record);
    }

    /**
     * 管理员强制归还图书。
     * <p>
     * 管理员可不校验用户身份直接强制归还指定借阅记录的图书，
     * 适用于用户线下还书或管理员介入的特殊场景。
     * 归还后自动计算逾期罚金并更新图书状态。
     * </p>
     *
     * @param recordId 借阅记录 ID
     * @return 更新后的借阅记录视图对象
     * @throws BusinessException 当记录不存在时抛出 404 异常，
     *         当记录状态不合法时抛出 409 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecordVO adminReturnRecord(Long recordId) {
        // 1. 管理员直接按记录 ID 查询（不校验用户身份），适用于线下还书等场景
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, ErrorCode.RECORD_NOT_FOUND);
        }
        // 2. 调用核心归还逻辑
        return doReturn(record);
    }

    /**
     * 执行归还核心逻辑。
     * <p>
     * 校验借阅记录状态（仅 BORROWED 或 RENEWED 可归还），
     * 计算逾期天数及罚金（若逾期），更新借阅记录状态为 RETURNED，
     * 并将对应图书状态恢复为 AVAILABLE。
     * 该方法为 returnBook 和 adminReturnRecord 的共用逻辑。
     * </p>
     *
     * @param record 借阅记录实体
     * @return 更新后的借阅记录视图对象
     * @throws BusinessException 当记录状态不合法时抛出 409 异常
     */
    private BorrowRecordVO doReturn(BorrowRecord record) {
        // 1. 校验借阅记录状态：仅 BORROWED（借阅中）或 RENEWED（已续借）允许归还
        String status = record.getStatus();
        if (!BorrowStatus.BORROWED.name().equals(status) && !BorrowStatus.RENEWED.name().equals(status)) {
            throw new BusinessException(409, ErrorCode.INVALID_STATUS);
        }

        // 2. 记录实际归还日期，更新记录状态为 RETURNED
        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        record.setStatus(BorrowStatus.RETURNED.name());

        // 3. 计算逾期罚金：若实际归还日期晚于应还日期，按 0.10 元/天计算
        if (today.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
            BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays))
                    .setScale(2, RoundingMode.HALF_UP);
            record.setFineAmount(fine);
        }

        // 4. 持久化更新借阅记录
        borrowRecordMapper.updateById(record);

        // 5. 原子化恢复可用库存，并更新图书状态
        bookMapper.incrementAvailableStock(record.getBookId());
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null && book.getAvailableStock() == 1) {
            book.setStatus(BookStatus.AVAILABLE.name());
            bookMapper.updateById(book);
        }

        // 6. 查询用户信息并组装视图对象返回
        User user = userMapper.selectById(record.getUserId());
        return buildVO(record, book, user);
    }

    /**
     * 用户续借图书。
     * <p>
     * 校验借阅记录归属当前用户，校验记录状态为 BORROWED 或 RENEWED，
     * 校验续借次数是否已达上限（最多 1 次）。
     * 通过校验后续借日期延长 15 天，续借次数加 1，状态更新为 RENEWED。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param recordId 借阅记录 ID
     * @return 续借后的借阅记录视图对象
     * @throws BusinessException 当记录不存在时抛出 404 异常，
     *         当状态不合法或续借次数已达上限时抛出 409 异常
     */
    @Override
    @SentinelResource(value = "renew", fallback = "fallbackObject", fallbackClass = SentinelRuleInitializer.class)
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecordVO renew(Long userId, Long recordId) {
        // 1. 校验借阅记录归属：记录必须属于当前用户
        BorrowRecord record = borrowRecordMapper.selectOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getId, recordId)
                .eq(BorrowRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, ErrorCode.RECORD_NOT_FOUND);
        }

        // 2. 校验记录状态：仅 BORROWED（借阅中）状态允许续借
        String status = record.getStatus();
        if (!BorrowStatus.BORROWED.name().equals(status)) {
            throw new BusinessException(409, ErrorCode.INVALID_STATUS);
        }

        // 3. 校验续借次数：每本书最多续借 1 次，已达上限则拒绝
        if (record.getRenewCount() != null && record.getRenewCount() >= MAX_RENEW_COUNT) {
            throw new BusinessException(409, ErrorCode.RENEW_LIMIT_EXCEEDED);
        }

        // 4. 执行续借：应还日期延长 15 天，续借次数加 1，状态更新为 RENEWED
        record.setDueDate(record.getDueDate().plusDays(RENEW_DAYS));
        record.setRenewCount(record.getRenewCount() == null ? 1 : record.getRenewCount() + 1);
        record.setStatus(BorrowStatus.RENEWED.name());
        borrowRecordMapper.updateById(record);

        // 5. 查询关联信息并组装视图对象返回
        Book book = bookMapper.selectById(record.getBookId());
        User user = userMapper.selectById(record.getUserId());
        return buildVO(record, book, user);
    }

    /**
     * 查询当前用户的借阅记录。
     * <p>
     * 根据用户 ID 查询借阅记录，支持按借阅状态筛选，
     * 结果按创建时间倒序排列。每条记录附带对应的图书名和用户名信息。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    分页查询参数，支持按状态筛选
     * @return 借阅记录分页结果
     */
    @Override
    @SentinelResource(value = "myBorrows", fallback = "fallbackPageResult", fallbackClass = SentinelRuleInitializer.class)
    public PageResult<BorrowRecordVO> myBorrows(Long userId, BorrowPageReq req) {
        // 1. 构建查询条件：按当前用户 ID 精确过滤，可选按借阅状态进一步筛选
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId);
        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, req.getStatus());
        }
        // 2. 按创建时间倒序排列，最新记录优先展示
        wrapper.orderByDesc(BorrowRecord::getCreatedAt);

        // 3. 执行分页查询
        Page<BorrowRecord> page = new Page<>(req.getPage(), req.getSize());
        IPage<BorrowRecord> result = borrowRecordMapper.selectPage(page, wrapper);

        // 4. 流式转换为 VO：每条记录需查询对应的图书名和用户名
        List<BorrowRecordVO> vos = result.getRecords().stream()
                .map(record -> {
                    Book book = bookMapper.selectById(record.getBookId());
                    User user = userMapper.selectById(record.getUserId());
                    return buildVO(record, book, user);
                }).toList();

        // 5. 组装分页结果返回
        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 管理员分页查询所有借阅记录。
     * <p>
     * 支持按借阅状态、用户 ID 筛选；支持关键词搜索，关键词同时匹配
     * 用户名和图书名，先分别查询用户表和图书表获取匹配的 ID 列表，
     * 再组合为 OR 条件进行借阅记录筛选。
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param req 管理员借阅分页查询参数
     * @return 所有借阅记录的分页结果
     */
    @Override
    public PageResult<BorrowRecordVO> adminPage(AdminBorrowPageReq req) {
        // 1. 构建基础过滤条件：按借阅状态和用户 ID 精确筛选
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();

        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, req.getStatus());
        }
        if (req.getUserId() != null) {
            wrapper.eq(BorrowRecord::getUserId, req.getUserId());
        }

        // 2. 关键词搜索：关键词同时匹配用户名和图书名
        String keyword = req.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            // 先分别查询用户表和图书表，获取匹配的 ID 列表
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .like(User::getUsername, keyword)
                            .select(User::getId))
                    .stream().map(User::getId).toList();
            List<Long> bookIds = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                            .like(Book::getTitle, keyword)
                            .select(Book::getId))
                    .stream().map(Book::getId).toList();

            // 若关键词没有匹配到任何用户或图书，直接返回空分页
            if (userIds.isEmpty() && bookIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L, (long) req.getPage(), (long) req.getSize());
            }

            // 组合 OR 条件：用户 ID 匹配 OR 图书 ID 匹配
            final List<Long> finalUserIds = userIds;
            final List<Long> finalBookIds = bookIds;
            wrapper.and(w -> {
                if (!finalUserIds.isEmpty()) {
                    w.in(BorrowRecord::getUserId, finalUserIds);
                }
                if (!finalUserIds.isEmpty() && !finalBookIds.isEmpty()) {
                    w.or();
                }
                if (!finalBookIds.isEmpty()) {
                    w.in(BorrowRecord::getBookId, finalBookIds);
                }
            });
        }

        // 3. 按创建时间倒序排列
        wrapper.orderByDesc(BorrowRecord::getCreatedAt);

        // 4. 执行分页查询
        Page<BorrowRecord> page = new Page<>(req.getPage(), req.getSize());
        IPage<BorrowRecord> result = borrowRecordMapper.selectPage(page, wrapper);

        // 5. 流式转换为 VO（附带图书名和用户名）
        List<BorrowRecordVO> vos = result.getRecords().stream()
                .map(record -> {
                    Book book = bookMapper.selectById(record.getBookId());
                    User user = userMapper.selectById(record.getUserId());
                    return buildVO(record, book, user);
                }).toList();

        // 6. 组装分页结果返回
        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 构建借阅记录视图对象。
     * <p>
     * 将借阅记录实体与关联的图书、用户信息组装为视图对象。
     * 图书或用户可能为空（如已被删除），此时对应的名称字段置为 null。
     * </p>
     *
     * @param record 借阅记录实体
     * @param book   关联的图书实体（可能为 null）
     * @param user   关联的用户实体（可能为 null）
     * @return 借阅记录视图对象
     */
    private BorrowRecordVO buildVO(BorrowRecord record, Book book, User user) {
        // 将借阅记录与关联的图书、用户信息组装为视图对象
        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setUsername(user != null ? user.getUsername() : null);   // 用户可能已被删除，此时用户名为 null
        vo.setBookId(record.getBookId());
        vo.setBookTitle(book != null ? book.getTitle() : null);     // 图书可能已被删除，此时书名为 null
        vo.setBorrowDate(record.getBorrowDate());
        vo.setDueDate(record.getDueDate());
        vo.setReturnDate(record.getReturnDate());
        vo.setStatus(record.getStatus());
        vo.setRenewCount(record.getRenewCount());
        vo.setFineAmount(record.getFineAmount());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
