package com.zjw.booknexus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
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
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.vo.BookVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookCategoryRelMapper bookCategoryRelMapper;
    private final BookshelfMapper bookshelfMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<BookVO> page(BookPageReq req) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getKeyword())) {
            wrapper.and(w -> w.like(Book::getTitle, req.getKeyword())
                    .or().like(Book::getAuthor, req.getKeyword())
                    .or().like(Book::getIsbn, req.getKeyword()));
        }
        if (StrUtil.isNotBlank(req.getStatus())) {
            wrapper.eq(Book::getStatus, req.getStatus());
        }
        if (req.getBookshelfId() != null) {
            wrapper.eq(Book::getBookshelfId, req.getBookshelfId());
        }
        wrapper.orderByDesc(Book::getCreatedAt);

        Page<Book> mpPage = new Page<>(req.getPage(), req.getSize());
        Page<Book> result = bookMapper.selectPage(mpPage, wrapper);

        List<BookVO> voList = result.getRecords().stream()
                .map(this::toBookVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public BookVO getById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        return toBookVO(book);
    }

    @Override
    @Transactional
    public BookVO create(BookCreateReq req) {
        checkDuplicateIsbn(req.getIsbn(), null);

        Book book = new Book();
        BeanUtil.copyProperties(req, book);
        book.setPublishDate(req.getPublishedDate());
        book.setStatus("AVAILABLE");
        book.setStock(1);
        book.setAvailableStock(1);

        bookMapper.insert(book);
        saveCategoryRelations(book.getId(), req.getCategoryIds());

        return toBookVO(book);
    }

    @Override
    @Transactional
    public BookVO update(Long id, BookUpdateReq req) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(req.getIsbn()) && !req.getIsbn().equals(book.getIsbn())) {
            checkDuplicateIsbn(req.getIsbn(), id);
            book.setIsbn(req.getIsbn());
        }
        if (StrUtil.isNotBlank(req.getTitle())) {
            book.setTitle(req.getTitle());
        }
        if (StrUtil.isNotBlank(req.getAuthor())) {
            book.setAuthor(req.getAuthor());
        }
        if (StrUtil.isNotBlank(req.getPublisher())) {
            book.setPublisher(req.getPublisher());
        }
        if (req.getDescription() != null) {
            book.setDescription(req.getDescription());
        }
        if (req.getCoverUrl() != null) {
            book.setCoverUrl(req.getCoverUrl());
        }
        if (req.getPublishedDate() != null) {
            book.setPublishDate(req.getPublishedDate());
        }
        if (req.getBookshelfId() != null) {
            book.setBookshelfId(req.getBookshelfId());
        }

        bookMapper.updateById(book);

        if (req.getCategoryIds() != null) {
            LambdaQueryWrapper<BookCategoryRel> relWrapper = new LambdaQueryWrapper<>();
            relWrapper.eq(BookCategoryRel::getBookId, id);
            bookCategoryRelMapper.delete(relWrapper);
            saveCategoryRelations(id, req.getCategoryIds());
        }

        return toBookVO(bookMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        bookMapper.deleteById(id);
    }

    private void checkDuplicateIsbn(String isbn, Long excludeId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsbn, isbn);
        if (excludeId != null) {
            wrapper.ne(Book::getId, excludeId);
        }
        if (bookMapper.selectOne(wrapper) != null) {
            throw new BusinessException(409, ErrorCode.DUPLICATE_ISBN);
        }
    }

    private void saveCategoryRelations(Long bookId, List<Long> categoryIds) {
        if (CollectionUtil.isEmpty(categoryIds)) {
            return;
        }
        for (Long categoryId : categoryIds) {
            BookCategoryRel rel = new BookCategoryRel();
            rel.setBookId(bookId);
            rel.setCategoryId(categoryId);
            bookCategoryRelMapper.insert(rel);
        }
    }

    private BookVO toBookVO(Book book) {
        BookVO vo = new BookVO();
        BeanUtil.copyProperties(book, vo);
        vo.setPublishDate(book.getPublishDate());

        LambdaQueryWrapper<BookCategoryRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(BookCategoryRel::getBookId, book.getId());
        List<BookCategoryRel> rels = bookCategoryRelMapper.selectList(relWrapper);
        if (CollectionUtil.isNotEmpty(rels)) {
            List<Long> categoryIds = rels.stream().map(BookCategoryRel::getCategoryId).toList();
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            vo.setCategoryNames(categories.stream().map(Category::getName).toList());
        } else {
            vo.setCategoryNames(List.of());
        }

        if (book.getBookshelfId() != null) {
            Bookshelf bookshelf = bookshelfMapper.selectById(book.getBookshelfId());
            if (bookshelf != null) {
                vo.setBookshelfName(bookshelf.getName());
            }
        }

        return vo;
    }
}
