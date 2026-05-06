package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookshelfCreateReq;
import com.zjw.booknexus.dto.BookshelfPageReq;
import com.zjw.booknexus.dto.BookshelfUpdateReq;
import com.zjw.booknexus.entity.Bookshelf;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookshelfMapper;
import com.zjw.booknexus.service.impl.BookshelfServiceImpl;
import com.zjw.booknexus.vo.BookshelfVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("书架服务单元测试")
@ExtendWith(MockitoExtension.class)
class BookshelfServiceTest {

    @Mock
    private BookshelfMapper bookshelfMapper;

    @InjectMocks
    private BookshelfServiceImpl bookshelfService;

    private Bookshelf buildBookshelf(Long id, String name) {
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setId(id);
        bookshelf.setName(name);
        bookshelf.setLocation("Floor 1");
        bookshelf.setDescription("Test shelf");
        return bookshelf;
    }

    @Test
    void shouldReturnPage_whenQueryWithKeyword() {
        BookshelfPageReq req = new BookshelfPageReq();
        req.setKeyword("A");

        Bookshelf shelf = buildBookshelf(1L, "Shelf A");
        Page<Bookshelf> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(shelf));
        pageResult.setTotal(1);

        when(bookshelfMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        PageResult<BookshelfVO> result = bookshelfService.page(req);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("Shelf A", result.getRecords().get(0).getName());
    }

    @Test
    void shouldReturnAllBookshelves_whenListAll() {
        Bookshelf s1 = buildBookshelf(1L, "A");
        Bookshelf s2 = buildBookshelf(2L, "B");

        when(bookshelfMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(s1, s2));

        List<BookshelfVO> result = bookshelfService.listAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnBookshelf_whenGetByIdExists() {
        Bookshelf shelf = buildBookshelf(1L, "Shelf A");
        when(bookshelfMapper.selectById(1L)).thenReturn(shelf);

        BookshelfVO vo = bookshelfService.getById(1L);

        assertNotNull(vo);
        assertEquals("Shelf A", vo.getName());
    }

    @Test
    void shouldThrow_whenGetByIdNotFound() {
        when(bookshelfMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookshelfService.getById(99L));
        assertEquals(404, ex.getCode());
        assertEquals(ErrorCode.BOOKSHELF_NOT_FOUND, ex.getMessage());
    }

    @Test
    void shouldCreateBookshelf_whenNameIsUnique() {
        BookshelfCreateReq req = new BookshelfCreateReq();
        req.setName("New Shelf");
        req.setLocation("Floor 2");

        when(bookshelfMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        doAnswer(inv -> {
            Bookshelf b = inv.getArgument(0);
            b.setId(1L);
            return 1;
        }).when(bookshelfMapper).insert(any(Bookshelf.class));

        BookshelfVO vo = bookshelfService.create(req);

        assertNotNull(vo);
        assertEquals("New Shelf", vo.getName());
        verify(bookshelfMapper).insert(any(Bookshelf.class));
    }

    @Test
    void shouldThrow_whenCreateBookshelfWithDuplicateName() {
        BookshelfCreateReq req = new BookshelfCreateReq();
        req.setName("Existing");

        when(bookshelfMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Bookshelf());

        BusinessException ex = assertThrows(BusinessException.class, () -> bookshelfService.create(req));
        assertEquals(409, ex.getCode());
        verify(bookshelfMapper, never()).insert(any(Bookshelf.class));
    }

    @Test
    void shouldUpdateBookshelf_whenExistsAndNameUnique() {
        Bookshelf existing = buildBookshelf(1L, "Old Shelf");
        existing.setLocation("Floor 1");

        BookshelfUpdateReq req = new BookshelfUpdateReq();
        req.setName("New Shelf");
        req.setLocation("Floor 3");

        when(bookshelfMapper.selectById(1L)).thenReturn(existing);
        when(bookshelfMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(bookshelfMapper.updateById(any(Bookshelf.class))).thenReturn(1);
        when(bookshelfMapper.selectById(1L)).thenReturn(existing);

        BookshelfVO vo = bookshelfService.update(1L, req);

        assertNotNull(vo);
        verify(bookshelfMapper).updateById(any(Bookshelf.class));
    }

    @Test
    void shouldUpdateBookshelf_whenOnlyLocationProvided() {
        Bookshelf existing = buildBookshelf(1L, "Shelf A");
        existing.setLocation("Floor 1");

        BookshelfUpdateReq req = new BookshelfUpdateReq();
        req.setLocation("Floor 2");

        when(bookshelfMapper.selectById(1L)).thenReturn(existing);
        when(bookshelfMapper.updateById(any(Bookshelf.class))).thenReturn(1);
        when(bookshelfMapper.selectById(1L)).thenReturn(existing);

        BookshelfVO vo = bookshelfService.update(1L, req);

        assertNotNull(vo);
        verify(bookshelfMapper).updateById(any(Bookshelf.class));
    }

    @Test
    void shouldThrow_whenUpdateBookshelfNotFound() {
        when(bookshelfMapper.selectById(99L)).thenReturn(null);

        BookshelfUpdateReq req = new BookshelfUpdateReq();
        req.setName("New");

        BusinessException ex = assertThrows(BusinessException.class, () -> bookshelfService.update(99L, req));
        assertEquals(404, ex.getCode());
    }

    @Test
    void shouldDeleteBookshelf_whenExists() {
        Bookshelf shelf = buildBookshelf(1L, "ToDelete");
        when(bookshelfMapper.selectById(1L)).thenReturn(shelf);
        when(bookshelfMapper.deleteById(1L)).thenReturn(1);

        bookshelfService.delete(1L);

        verify(bookshelfMapper).deleteById(1L);
    }

    @Test
    void shouldThrow_whenDeleteBookshelfNotFound() {
        when(bookshelfMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookshelfService.delete(99L));
        assertEquals(404, ex.getCode());
    }
}
