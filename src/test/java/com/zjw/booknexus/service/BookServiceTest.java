package com.zjw.booknexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookCreateReq;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.dto.BookUpdateReq;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.BookCategoryRel;
import com.zjw.booknexus.entity.Bookshelf;
import com.zjw.booknexus.entity.Category;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookCategoryRelMapper;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.BookshelfMapper;
import com.zjw.booknexus.mapper.CategoryMapper;
import com.zjw.booknexus.service.impl.BookServiceImpl;
import com.zjw.booknexus.vo.BookVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookServiceTest {

    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookCategoryRelMapper bookCategoryRelMapper;
    @Mock
    private BookshelfMapper bookshelfMapper;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book buildBook(Long id, String title, String isbn) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setPublisher("Publisher");
        book.setStatus("AVAILABLE");
        book.setStock(1);
        book.setAvailableStock(1);
        book.setBookshelfId(1L);
        book.setPublishDate(LocalDate.of(2024, 1, 1));
        return book;
    }

    @Test
    void pageBooks() {
        BookPageReq req = new BookPageReq();
        req.setPage(1);
        req.setSize(10);
        req.setKeyword("Spring");

        Book book = buildBook(1L, "Spring in Action", "978-001");
        Page<Book> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of(book));
        resultPage.setTotal(1);

        when(bookMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);
        when(bookCategoryRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        PageResult<BookVO> result = bookService.page(req);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Spring in Action", result.getRecords().get(0).getTitle());
    }

    @Test
    void pageBooksWithFilters() {
        BookPageReq req = new BookPageReq();
        req.setStatus("BORROWED");
        req.setBookshelfId(2L);

        Page<Book> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of());
        resultPage.setTotal(0);

        when(bookMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        PageResult<BookVO> result = bookService.page(req);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void getBookById() {
        Book book = buildBook(1L, "Clean Code", "978-002");
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Programming");
        Bookshelf shelf = new Bookshelf();
        shelf.setId(1L);
        shelf.setName("Shelf A");
        BookCategoryRel rel = new BookCategoryRel();
        rel.setBookId(1L);
        rel.setCategoryId(1L);

        when(bookMapper.selectById(1L)).thenReturn(book);
        when(bookCategoryRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rel));
        when(categoryMapper.selectBatchIds(anyList())).thenReturn(List.of(cat));
        when(bookshelfMapper.selectById(1L)).thenReturn(shelf);

        BookVO vo = bookService.getById(1L);

        assertNotNull(vo);
        assertEquals("Clean Code", vo.getTitle());
        assertEquals(List.of("Programming"), vo.getCategoryNames());
        assertEquals("Shelf A", vo.getBookshelfName());
    }

    @Test
    void getBookByIdNotFound() {
        when(bookMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.getById(99L));
        assertEquals(404, ex.getCode());
        assertTrue(ex.getMessage().contains("BOOK_NOT_FOUND"));
    }

    @Test
    void createBook() {
        BookCreateReq req = new BookCreateReq();
        req.setIsbn("978-003");
        req.setTitle("Effective Java");
        req.setAuthor("Josh Bloch");
        req.setPublisher("Addison-Wesley");
        req.setPublishedDate(LocalDate.of(2020, 5, 15));
        req.setBookshelfId(1L);
        req.setCategoryIds(List.of(1L, 2L));

        when(bookMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(bookMapper.insert(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1L);
            return 1;
        });
        lenient().when(bookCategoryRelMapper.insert(any(BookCategoryRel.class))).thenReturn(1);
        when(bookCategoryRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        Bookshelf shelf = new Bookshelf();
        shelf.setId(1L);
        shelf.setName("Shelf A");
        when(bookshelfMapper.selectById(1L)).thenReturn(shelf);

        BookVO vo = bookService.create(req);

        assertNotNull(vo);
        assertEquals("978-003", vo.getIsbn());
        assertEquals("Effective Java", vo.getTitle());
        assertEquals("AVAILABLE", vo.getStatus());
        assertEquals("Shelf A", vo.getBookshelfName());

        verify(bookCategoryRelMapper, org.mockito.Mockito.times(2)).insert(any(BookCategoryRel.class));
    }

    @Test
    void createBookDuplicateIsbn() {
        BookCreateReq req = new BookCreateReq();
        req.setIsbn("978-003");
        req.setTitle("Some Book");
        req.setAuthor("Author");
        req.setPublisher("Publisher");

        when(bookMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Book());

        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.create(req));
        assertEquals(409, ex.getCode());
        assertTrue(ex.getMessage().contains("DUPLICATE_ISBN"));

        verify(bookMapper, never()).insert(any(Book.class));
    }

    @Test
    void updateBook() {
        Book existing = buildBook(1L, "Old Title", "978-004");
        BookUpdateReq req = new BookUpdateReq();
        req.setTitle("New Title");
        req.setCategoryIds(List.of(3L));

        when(bookMapper.selectById(1L)).thenReturn(existing);
        when(bookMapper.updateById(any(Book.class))).thenReturn(1);
        when(bookCategoryRelMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        lenient().when(bookCategoryRelMapper.insert(any(BookCategoryRel.class))).thenReturn(1);
        when(bookCategoryRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(bookshelfMapper.selectById(1L)).thenReturn(null);

        BookVO vo = bookService.update(1L, req);

        assertNotNull(vo);
        assertEquals("New Title", vo.getTitle());
        assertEquals("978-004", vo.getIsbn());
    }

    @Test
    void updateBookNotFound() {
        BookUpdateReq req = new BookUpdateReq();
        req.setTitle("New Title");

        when(bookMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.update(99L, req));
        assertEquals(404, ex.getCode());
    }

    @Test
    void deleteBook() {
        Book book = buildBook(1L, "To Delete", "978-005");
        when(bookMapper.selectById(1L)).thenReturn(book);
        when(bookMapper.deleteById(1L)).thenReturn(1);

        bookService.delete(1L);

        verify(bookMapper).deleteById(1L);
    }

    @Test
    void deleteBookNotFound() {
        when(bookMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.delete(99L));
        assertEquals(404, ex.getCode());
    }
}
