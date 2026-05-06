package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.SubscriptionReq;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.Subscription;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.SubscriptionMapper;
import com.zjw.booknexus.service.impl.SubscriptionServiceImpl;
import com.zjw.booknexus.vo.SubscriptionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("订阅服务单元测试")
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionMapper subscriptionMapper;
    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private static final Long USER_ID = 1L;
    private static final Long BOOK_ID = 100L;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(BOOK_ID);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setCoverUrl("http://cover.url/test.jpg");
    }

    @Test
    void shouldSubscribe_whenBookExistsAndNotSubscribed() {
        SubscriptionReq req = new SubscriptionReq();
        req.setBookId(BOOK_ID);

        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(subscriptionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId(1L);
            return 1;
        }).when(subscriptionMapper).insert(any(Subscription.class));

        SubscriptionVO vo = subscriptionService.subscribe(USER_ID, req);

        assertNotNull(vo);
        assertEquals(USER_ID, vo.getUserId());
        assertEquals(BOOK_ID, vo.getBookId());
        assertEquals("Test Book", vo.getBookTitle());
        assertEquals(1, vo.getIsActive());
        verify(subscriptionMapper).insert(any(Subscription.class));
    }

    @Test
    void shouldThrow_whenSubscribeAndBookNotFound() {
        SubscriptionReq req = new SubscriptionReq();
        req.setBookId(999L);

        when(bookMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> subscriptionService.subscribe(USER_ID, req));
        assertEquals(404, ex.getCode());
        verify(subscriptionMapper, never()).insert(any(Subscription.class));
    }

    @Test
    void shouldThrow_whenSubscribeAndAlreadySubscribed() {
        SubscriptionReq req = new SubscriptionReq();
        req.setBookId(BOOK_ID);

        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(subscriptionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> subscriptionService.subscribe(USER_ID, req));
        assertEquals(409, ex.getCode());
        verify(subscriptionMapper, never()).insert(any(Subscription.class));
    }

    @Test
    void shouldUnsubscribe_whenActiveRecordExists() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUserId(USER_ID);
        subscription.setBookId(BOOK_ID);
        subscription.setIsActive(1);
        subscription.setCreatedAt(LocalDateTime.now());

        when(subscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(subscription);
        when(subscriptionMapper.updateById(any(Subscription.class))).thenReturn(1);

        subscriptionService.unsubscribe(USER_ID, BOOK_ID);

        assertEquals(0, subscription.getIsActive());
        assertNotNull(subscription.getUpdatedAt());
        verify(subscriptionMapper).updateById(any(Subscription.class));
    }

    @Test
    void shouldThrow_whenUnsubscribeAndRecordNotFound() {
        when(subscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> subscriptionService.unsubscribe(USER_ID, BOOK_ID));
        assertEquals(404, ex.getCode());
        verify(subscriptionMapper, never()).updateById(any(Subscription.class));
    }

    @Test
    void shouldReturnPage_whenQueryMySubscriptions() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUserId(USER_ID);
        subscription.setBookId(BOOK_ID);
        subscription.setIsActive(1);
        subscription.setCreatedAt(LocalDateTime.now());

        Page<Subscription> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(subscription));
        pageResult.setTotal(1);

        when(subscriptionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);

        PageResult<SubscriptionVO> result = subscriptionService.mySubscriptions(USER_ID, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("Test Book", result.getRecords().get(0).getBookTitle());
        assertEquals(1, result.getRecords().get(0).getIsActive());
    }

    @Test
    void shouldReturnTrue_whenIsSubscribed() {
        when(subscriptionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertTrue(subscriptionService.isSubscribed(USER_ID, BOOK_ID));
    }

    @Test
    void shouldReturnFalse_whenIsNotSubscribed() {
        when(subscriptionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(subscriptionService.isSubscribed(USER_ID, BOOK_ID));
    }

    @Test
    void shouldReturnFalse_whenIsSubscribedWithNullUserId() {
        assertFalse(subscriptionService.isSubscribed(null, BOOK_ID));
    }

    @Test
    void shouldReturnFalse_whenIsSubscribedWithNullBookId() {
        assertFalse(subscriptionService.isSubscribed(USER_ID, null));
    }
}
