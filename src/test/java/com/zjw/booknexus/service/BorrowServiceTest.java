package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.PageResult;
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
import com.zjw.booknexus.service.impl.BorrowServiceImpl;
import com.zjw.booknexus.vo.BorrowRecordVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordMapper borrowRecordMapper;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private static final Long USER_ID = 1L;
    private static final Long BOOK_ID = 100L;
    private static final Long RECORD_ID = 10L;

    private Book testBook;
    private User testUser;
    private BorrowRecord testRecord;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(BOOK_ID);
        testBook.setTitle("Test Book");
        testBook.setStatus(BookStatus.AVAILABLE.name());
        testBook.setStock(1);
        testBook.setAvailableStock(1);

        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername("testuser");

        testRecord = new BorrowRecord();
        testRecord.setId(RECORD_ID);
        testRecord.setUserId(USER_ID);
        testRecord.setBookId(BOOK_ID);
        testRecord.setBorrowDate(LocalDate.now().minusDays(5));
        testRecord.setDueDate(LocalDate.now().plusDays(25));
        testRecord.setReturnDate(null);
        testRecord.setStatus(BorrowStatus.BORROWED.name());
        testRecord.setRenewCount(0);
        testRecord.setFineAmount(BigDecimal.ZERO);
        testRecord.setCreatedAt(LocalDateTime.now().minusDays(5));

        lenient().when(userMapper.selectById(USER_ID)).thenReturn(testUser);
    }

    @Test
    void borrowSuccessfully() {
        BorrowReq req = new BorrowReq();
        req.setBookId(BOOK_ID);

        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(borrowRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doAnswer(inv -> {
            BorrowRecord r = inv.getArgument(0);
            r.setId(RECORD_ID);
            return 1;
        }).when(borrowRecordMapper).insert(any(BorrowRecord.class));
        when(bookMapper.updateById(any(Book.class))).thenReturn(1);

        BorrowRecordVO vo = borrowService.borrow(USER_ID, req);

        assertNotNull(vo);
        assertEquals(USER_ID, vo.getUserId());
        assertEquals(BOOK_ID, vo.getBookId());
        assertEquals(BorrowStatus.BORROWED.name(), vo.getStatus());
        assertEquals(0, vo.getRenewCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(vo.getFineAmount()));

        verify(borrowRecordMapper).insert(any(BorrowRecord.class));
        verify(bookMapper).updateById(any(Book.class));
    }

    @Test
    void borrowBookNotFound() {
        BorrowReq req = new BorrowReq();
        req.setBookId(999L);

        when(bookMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrow(USER_ID, req));
        assertEquals(404, ex.getCode());

        verify(borrowRecordMapper, never()).insert(any(BorrowRecord.class));
    }

    @Test
    void borrowBookNotAvailable() {
        BorrowReq req = new BorrowReq();
        req.setBookId(BOOK_ID);

        testBook.setStatus(BookStatus.BORROWED.name());
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrow(USER_ID, req));
        assertEquals(409, ex.getCode());

        verify(borrowRecordMapper, never()).insert(any(BorrowRecord.class));
    }

    @Test
    void borrowExceedsLimit() {
        BorrowReq req = new BorrowReq();
        req.setBookId(BOOK_ID);

        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(borrowRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrow(USER_ID, req));
        assertEquals(409, ex.getCode());

        verify(borrowRecordMapper, never()).insert(any(BorrowRecord.class));
    }

    @Test
    void renewSuccessfully() {
        LocalDate originalDueDate = testRecord.getDueDate();
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRecord);
        when(borrowRecordMapper.updateById(any(BorrowRecord.class))).thenReturn(1);
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);

        BorrowRecordVO vo = borrowService.renew(USER_ID, RECORD_ID);

        assertNotNull(vo);
        assertEquals(BorrowStatus.RENEWED.name(), vo.getStatus());
        assertEquals(1, vo.getRenewCount());
        assertEquals(originalDueDate.plusDays(15), vo.getDueDate());
    }

    @Test
    void renewRecordNotFound() {
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.renew(USER_ID, RECORD_ID));
        assertEquals(404, ex.getCode());
    }

    @Test
    void renewExceedsLimit() {
        testRecord.setRenewCount(1);
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRecord);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.renew(USER_ID, RECORD_ID));
        assertEquals(409, ex.getCode());

        verify(borrowRecordMapper, never()).updateById(any(BorrowRecord.class));
    }

    @Test
    void renewInvalidStatus() {
        testRecord.setStatus(BorrowStatus.RETURNED.name());
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRecord);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.renew(USER_ID, RECORD_ID));
        assertEquals(409, ex.getCode());
    }

    @Test
    void returnBookSuccessfullyNoFine() {
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRecord);
        when(borrowRecordMapper.updateById(any(BorrowRecord.class))).thenReturn(1);
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(bookMapper.updateById(any(Book.class))).thenReturn(1);

        BorrowRecordVO vo = borrowService.returnBook(USER_ID, RECORD_ID);

        assertNotNull(vo);
        assertEquals(BorrowStatus.RETURNED.name(), vo.getStatus());
        assertNotNull(vo.getReturnDate());
        assertEquals(0, BigDecimal.ZERO.compareTo(vo.getFineAmount()));
    }

    @Test
    void returnBookWithFine() {
        testRecord.setDueDate(LocalDate.now().minusDays(3));

        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRecord);
        when(borrowRecordMapper.updateById(any(BorrowRecord.class))).thenReturn(1);
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(bookMapper.updateById(any(Book.class))).thenReturn(1);

        BorrowRecordVO vo = borrowService.returnBook(USER_ID, RECORD_ID);

        assertNotNull(vo);
        assertEquals(BorrowStatus.RETURNED.name(), vo.getStatus());

        long overdueDays = ChronoUnit.DAYS.between(testRecord.getDueDate(), LocalDate.now());
        BigDecimal expectedFine = new BigDecimal("0.10")
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, expectedFine.compareTo(vo.getFineAmount()));
    }

    @Test
    void returnRecordNotFound() {
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.returnBook(USER_ID, RECORD_ID));
        assertEquals(404, ex.getCode());
    }

    @Test
    void returnInvalidStatus() {
        testRecord.setStatus(BorrowStatus.RETURNED.name());
        when(borrowRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRecord);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.returnBook(USER_ID, RECORD_ID));
        assertEquals(409, ex.getCode());
    }

    @Test
    void myBorrowsReturnsPage() {
        when(borrowRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<BorrowRecord>(1, 10).setRecords(java.util.List.of(testRecord)));
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);

        com.zjw.booknexus.dto.BorrowPageReq req = new com.zjw.booknexus.dto.BorrowPageReq();
        PageResult<BorrowRecordVO> result = borrowService.myBorrows(USER_ID, req);

        assertEquals(1, result.getRecords().size());
        assertEquals("Test Book", result.getRecords().get(0).getBookTitle());
    }

    @Test
    void adminReturnRecordSuccessfully() {
        BorrowRecord someoneElseRecord = new BorrowRecord();
        someoneElseRecord.setId(20L);
        someoneElseRecord.setUserId(2L);
        someoneElseRecord.setBookId(BOOK_ID);
        someoneElseRecord.setBorrowDate(LocalDate.now().minusDays(5));
        someoneElseRecord.setDueDate(LocalDate.now().plusDays(25));
        someoneElseRecord.setStatus(BorrowStatus.BORROWED.name());
        someoneElseRecord.setRenewCount(0);
        someoneElseRecord.setFineAmount(BigDecimal.ZERO);

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(borrowRecordMapper.selectById(20L)).thenReturn(someoneElseRecord);
        when(borrowRecordMapper.updateById(any(BorrowRecord.class))).thenReturn(1);
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(bookMapper.updateById(any(Book.class))).thenReturn(1);
        lenient().when(userMapper.selectById(2L)).thenReturn(otherUser);

        BorrowRecordVO vo = borrowService.adminReturnRecord(20L);

        assertNotNull(vo);
        assertEquals(BorrowStatus.RETURNED.name(), vo.getStatus());
        assertEquals(2L, vo.getUserId());
    }
}
