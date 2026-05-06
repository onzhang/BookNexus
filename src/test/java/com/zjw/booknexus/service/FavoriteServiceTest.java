package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.FavoriteReq;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.Favorite;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.FavoriteMapper;
import com.zjw.booknexus.service.impl.FavoriteServiceImpl;
import com.zjw.booknexus.vo.FavoriteVO;
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

@DisplayName("收藏服务单元测试")
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteMapper favoriteMapper;
    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

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
        testBook.setStatus("AVAILABLE");
    }

    @Test
    void shouldAddFavorite_whenBookExistsAndNotFavorited() {
        FavoriteReq req = new FavoriteReq();
        req.setBookId(BOOK_ID);

        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doAnswer(inv -> {
            Favorite f = inv.getArgument(0);
            f.setId(1L);
            return 1;
        }).when(favoriteMapper).insert(any(Favorite.class));

        FavoriteVO vo = favoriteService.addFavorite(USER_ID, req);

        assertNotNull(vo);
        assertEquals(USER_ID, vo.getUserId());
        assertEquals(BOOK_ID, vo.getBookId());
        assertEquals("Test Book", vo.getBookTitle());
        verify(favoriteMapper).insert(any(Favorite.class));
    }

    @Test
    void shouldThrow_whenAddFavoriteAndBookNotFound() {
        FavoriteReq req = new FavoriteReq();
        req.setBookId(999L);

        when(bookMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> favoriteService.addFavorite(USER_ID, req));
        assertEquals(404, ex.getCode());
        verify(favoriteMapper, never()).insert(any(Favorite.class));
    }

    @Test
    void shouldThrow_whenAddFavoriteAndAlreadyFavorited() {
        FavoriteReq req = new FavoriteReq();
        req.setBookId(BOOK_ID);

        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> favoriteService.addFavorite(USER_ID, req));
        assertEquals(409, ex.getCode());
        verify(favoriteMapper, never()).insert(any(Favorite.class));
    }

    @Test
    void shouldRemoveFavorite_whenRecordExists() {
        Favorite favorite = new Favorite();
        favorite.setId(1L);
        favorite.setUserId(USER_ID);
        favorite.setBookId(BOOK_ID);

        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(favorite);
        when(favoriteMapper.deleteById(1L)).thenReturn(1);

        favoriteService.removeFavorite(USER_ID, BOOK_ID);

        verify(favoriteMapper).deleteById(1L);
    }

    @Test
    void shouldThrow_whenRemoveFavoriteAndRecordNotFound() {
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> favoriteService.removeFavorite(USER_ID, BOOK_ID));
        assertEquals(404, ex.getCode());
        verify(favoriteMapper, never()).deleteById(anyLong());
    }

    @Test
    void shouldReturnPage_whenQueryMyFavorites() {
        Favorite favorite = new Favorite();
        favorite.setId(1L);
        favorite.setUserId(USER_ID);
        favorite.setBookId(BOOK_ID);
        favorite.setCreatedAt(LocalDateTime.now());

        Page<Favorite> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(favorite));
        pageResult.setTotal(1);

        when(favoriteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
        when(bookMapper.selectById(BOOK_ID)).thenReturn(testBook);

        PageResult<FavoriteVO> result = favoriteService.myFavorites(USER_ID, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("Test Book", result.getRecords().get(0).getBookTitle());
    }

    @Test
    void shouldReturnTrue_whenBookIsFavorited() {
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertTrue(favoriteService.isFavorited(USER_ID, BOOK_ID));
    }

    @Test
    void shouldReturnFalse_whenBookIsNotFavorited() {
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(favoriteService.isFavorited(USER_ID, BOOK_ID));
    }

    @Test
    void shouldReturnFalse_whenIsFavoritedWithNullUserId() {
        assertFalse(favoriteService.isFavorited(null, BOOK_ID));
    }

    @Test
    void shouldReturnFalse_whenIsFavoritedWithNullBookId() {
        assertFalse(favoriteService.isFavorited(USER_ID, null));
    }
}
