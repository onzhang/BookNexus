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

/**
 * 图书服务实现类，实现图书相关的完整业务逻辑。
 * <p>
 * 处理图书的分页搜索、详情查询、创建、更新和删除操作。
 * 创建和更新操作涉及 ISBN 唯一性校验、图书与分类的关联关系维护、
 * 以及书架名称等关联信息的装配。使用 Hutool BeanUtil 实现属性拷贝，
 * MyBatis-Plus 实现数据访问，事务注解确保数据一致性。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookCategoryRelMapper bookCategoryRelMapper;
    private final BookshelfMapper bookshelfMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 分页查询图书。
     * <p>
     * 构建动态查询条件：关键词模糊匹配（书名、作者、ISBN）、
     * 图书状态精确匹配、书架 ID 精确匹配。结果按创建时间倒序排列。
     * 查询结果流式转换为 BookVO，包含分类名称和书架名称等关联信息。
     * </p>
     *
     * @param req 分页查询参数
     * @return 图书分页结果，每个元素包含完整的分类和书架信息
     */
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

    /**
     * 根据 ID 查询图书详情。
     * <p>
     * 查询图书实体并转换为 BookVO，若图书不存在则抛出 404 异常。
     * 转换过程会查询并填充关联的分类名称列表和书架名称。
     * </p>
     *
     * @param id 图书 ID
     * @return 图书视图对象，包含分类名称和书架名称
     * @throws BusinessException 当图书不存在时抛出 404 异常
     */
    @Override
    public BookVO getById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        return toBookVO(book);
    }

    /**
     * 创建图书。
     * <p>
     * 校验 ISBN 唯一性，使用 BeanUtil 拷贝属性创建图书实体，
     * 设置初始状态为 AVAILABLE、库存为 1、可用库存为 1。
     * 持久化图书后根据分类 ID 列表建立图书-分类关联关系。
     * 整个操作在一个事务中完成。
     * </p>
     *
     * @param req 图书创建请求，包含图书基本信息及分类 ID 列表
     * @return 新创建的图书视图对象
     * @throws BusinessException 当 ISBN 已存在时抛出 409 异常
     */
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

    /**
     * 更新图书信息。
     * <p>
     * 根据 ID 查询待更新图书，依次对非空字段进行部分更新。
     * 若更新 ISBN，需校验新 ISBN 的唯一性（排除当前图书自身）。
     * 若提供分类 ID 列表，先删除原有图书-分类关联关系再重新建立。
     * 整个操作在一个事务中完成。
     * </p>
     *
     * @param id  图书 ID
     * @param req 图书更新请求，包含需要更新的字段
     * @return 更新后的图书视图对象（重新从数据库查询）
     * @throws BusinessException 当图书不存在时抛出 404 异常，
     *         当 ISBN 与其他图书重复时抛出 409 异常
     */
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

    /**
     * 删除图书。
     * <p>
     * 物理删除指定 ID 的图书记录，操作不可恢复。
     * 删除前校验图书是否存在。
     * </p>
     *
     * @param id 要删除的图书 ID
     * @throws BusinessException 当图书不存在时抛出 404 异常
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        bookMapper.deleteById(id);
    }

    /**
     * 校验 ISBN 唯一性。
     * <p>
     * 根据 ISBN 查询数据库，若存在记录则抛出重复异常。
     * excludeId 参数用于更新场景排除当前图书自身。
     * </p>
     *
     * @param isbn      ISBN 编号
     * @param excludeId 需要排除的图书 ID（更新时使用），新建时传 null
     * @throws BusinessException 当 ISBN 已存在时抛出 409 异常
     */
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

    /**
     * 保存图书-分类关联关系。
     * <p>
     * 遍历分类 ID 列表，逐条插入关联记录。
     * 若分类 ID 列表为空则直接返回不执行任何操作。
     * </p>
     *
     * @param bookId      图书 ID
     * @param categoryIds 分类 ID 列表
     */
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

    /**
     * 将图书实体转换为视图对象。
     * <p>
     * 使用 BeanUtil 拷贝基础属性，查询并填充关联的分类名称列表和书架名称。
     * 若图书无分类或未关联书架，对应的字段置为空列表或 null。
     * </p>
     *
     * @param book 图书实体
     * @return 图书视图对象，包含完整的关联信息
     */
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
