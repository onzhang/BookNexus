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

    @Override
    @Transactional
    public BorrowRecordVO borrow(Long userId, BorrowReq req) {
        Book book = bookMapper.selectById(req.getBookId());
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        if (!BookStatus.AVAILABLE.name().equals(book.getStatus())) {
            throw new BusinessException(409, ErrorCode.BOOK_NOT_AVAILABLE);
        }

        long activeCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .in(BorrowRecord::getStatus, BorrowStatus.BORROWED.name(), BorrowStatus.RENEWED.name()));
        if (activeCount >= MAX_ACTIVE_BORROWS) {
            throw new BusinessException(409, ErrorCode.BORROW_LIMIT_EXCEEDED);
        }

        long existingBorrow = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, req.getBookId())
                .in(BorrowRecord::getStatus, BorrowStatus.BORROWED.name(), BorrowStatus.RENEWED.name()));
        if (existingBorrow > 0) {
            throw new BusinessException(409, ErrorCode.ALREADY_BORROWED);
        }

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

        book.setStatus(BookStatus.BORROWED.name());
        bookMapper.updateById(book);

        User user = userMapper.selectById(userId);
        return buildVO(record, book, user);
    }

    @Override
    @Transactional
    public BorrowRecordVO returnBook(Long userId, Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getId, recordId)
                .eq(BorrowRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, ErrorCode.RECORD_NOT_FOUND);
        }
        return doReturn(record);
    }

    @Override
    @Transactional
    public BorrowRecordVO adminReturnRecord(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, ErrorCode.RECORD_NOT_FOUND);
        }
        return doReturn(record);
    }

    private BorrowRecordVO doReturn(BorrowRecord record) {
        String status = record.getStatus();
        if (!BorrowStatus.BORROWED.name().equals(status) && !BorrowStatus.RENEWED.name().equals(status)) {
            throw new BusinessException(409, ErrorCode.INVALID_STATUS);
        }

        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        record.setStatus(BorrowStatus.RETURNED.name());

        if (today.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
            BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays))
                    .setScale(2, RoundingMode.HALF_UP);
            record.setFineAmount(fine);
        }

        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setStatus(BookStatus.AVAILABLE.name());
            bookMapper.updateById(book);
        }

        User user = userMapper.selectById(record.getUserId());
        return buildVO(record, book, user);
    }

    @Override
    @Transactional
    public BorrowRecordVO renew(Long userId, Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getId, recordId)
                .eq(BorrowRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, ErrorCode.RECORD_NOT_FOUND);
        }

        String status = record.getStatus();
        if (!BorrowStatus.BORROWED.name().equals(status) && !BorrowStatus.RENEWED.name().equals(status)) {
            throw new BusinessException(409, ErrorCode.INVALID_STATUS);
        }

        if (record.getRenewCount() != null && record.getRenewCount() >= MAX_RENEW_COUNT) {
            throw new BusinessException(409, ErrorCode.RENEW_LIMIT_EXCEEDED);
        }

        record.setDueDate(record.getDueDate().plusDays(RENEW_DAYS));
        record.setRenewCount(record.getRenewCount() == null ? 1 : record.getRenewCount() + 1);
        record.setStatus(BorrowStatus.RENEWED.name());
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        User user = userMapper.selectById(record.getUserId());
        return buildVO(record, book, user);
    }

    @Override
    public PageResult<BorrowRecordVO> myBorrows(Long userId, BorrowPageReq req) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId);
        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(BorrowRecord::getCreatedAt);

        Page<BorrowRecord> page = new Page<>(req.getPage(), req.getSize());
        IPage<BorrowRecord> result = borrowRecordMapper.selectPage(page, wrapper);

        List<BorrowRecordVO> vos = result.getRecords().stream()
                .map(record -> {
                    Book book = bookMapper.selectById(record.getBookId());
                    User user = userMapper.selectById(record.getUserId());
                    return buildVO(record, book, user);
                }).toList();

        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<BorrowRecordVO> adminPage(AdminBorrowPageReq req) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();

        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, req.getStatus());
        }
        if (req.getUserId() != null) {
            wrapper.eq(BorrowRecord::getUserId, req.getUserId());
        }

        String keyword = req.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .like(User::getUsername, keyword)
                            .select(User::getId))
                    .stream().map(User::getId).toList();
            List<Long> bookIds = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                            .like(Book::getTitle, keyword)
                            .select(Book::getId))
                    .stream().map(Book::getId).toList();

            if (userIds.isEmpty() && bookIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L, (long) req.getPage(), (long) req.getSize());
            }

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

        wrapper.orderByDesc(BorrowRecord::getCreatedAt);

        Page<BorrowRecord> page = new Page<>(req.getPage(), req.getSize());
        IPage<BorrowRecord> result = borrowRecordMapper.selectPage(page, wrapper);

        List<BorrowRecordVO> vos = result.getRecords().stream()
                .map(record -> {
                    Book book = bookMapper.selectById(record.getBookId());
                    User user = userMapper.selectById(record.getUserId());
                    return buildVO(record, book, user);
                }).toList();

        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private BorrowRecordVO buildVO(BorrowRecord record, Book book, User user) {
        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setUsername(user != null ? user.getUsername() : null);
        vo.setBookId(record.getBookId());
        vo.setBookTitle(book != null ? book.getTitle() : null);
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
