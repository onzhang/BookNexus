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
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zjw.booknexus.sentinel.SentinelRuleInitializer;
import com.zjw.booknexus.service.BookService;
import com.zjw.booknexus.vo.BookVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @SentinelResource(value = "bookPage", fallback = "fallback", fallbackClass = SentinelRuleInitializer.class)
    public PageResult<BookVO> page(BookPageReq req) {
        // 1. 构建动态查询条件：支持关键词、状态、书架 ID 三种筛选维度
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getKeyword())) {
            // 关键词同时匹配书名、作者、ISBN 三个字段，任一匹配即命中
            wrapper.and(w -> w.like(Book::getTitle, req.getKeyword())
                    .or().like(Book::getAuthor, req.getKeyword())
                    .or().like(Book::getIsbn, req.getKeyword()));
        }
        if (StrUtil.isNotBlank(req.getStatus())) {
            // 按图书状态精确筛选（AVAILABLE / BORROWED / DAMAGED / LOST）
            wrapper.eq(Book::getStatus, req.getStatus());
        }
        if (req.getBookshelfId() != null) {
            // 按书架位置精确筛选
            wrapper.eq(Book::getBookshelfId, req.getBookshelfId());
        }
        // 2. 按创建时间倒序排，最新上架的图书优先展示
        wrapper.orderByDesc(Book::getCreatedAt);

        // 3. 执行 MyBatis-Plus 分页查询
        Page<Book> mpPage = new Page<>(req.getPage(), req.getSize());
        Page<Book> result = bookMapper.selectPage(mpPage, wrapper);

        // 4. 将实体列表流式转换为视图对象（填充分类名、书架名等关联信息）
        List<BookVO> voList = result.getRecords().stream()
                .map(this::toBookVO)
                .toList();

        // 5. 组装分页结果返回
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
    @SentinelResource(value = "bookGetById", fallback = "fallback", fallbackClass = SentinelRuleInitializer.class)
    @Cacheable(value = "book", key = "#id")
    public BookVO getById(Long id) {
        // 1. 根据主键查询图书，不存在则抛出 404 异常
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        // 2. 转换为视图对象，附带分类和书架等关联信息
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
    @SentinelResource(value = "bookCreate", fallback = "fallback", fallbackClass = SentinelRuleInitializer.class)
    @Transactional
    public BookVO create(BookCreateReq req) {
        // 1. 校验 ISBN 唯一性：防止录入已存在的重复图书
        checkDuplicateIsbn(req.getIsbn(), null);

        // 2. 创建图书实体：使用 Hutool BeanUtil 批量拷贝同名属性
        Book book = new Book();
        BeanUtil.copyProperties(req, book);
        // 3. 设置默认值：新书上架状态为可借阅，库存从请求中获取
        book.setPublishDate(req.getPublishedDate());
        book.setStatus("AVAILABLE");
        book.setStock(req.getStock() != null ? req.getStock() : 1);
        book.setAvailableStock(req.getStock() != null ? req.getStock() : 1);

        // 4. 持久化图书记录
        bookMapper.insert(book);
        // 5. 建立图书与分类的关联关系（批量插入 book_category_rel 表）
        saveCategoryRelations(book.getId(), req.getCategoryIds());

        // 6. 返回创建后的图书视图（含 ID 及关联信息）
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
    @SentinelResource(value = "bookUpdate", fallback = "fallback", fallbackClass = SentinelRuleInitializer.class)
    @CacheEvict(value = "book", key = "#id")
    @Transactional
    public BookVO update(Long id, BookUpdateReq req) {
        // 1. 查询待更新图书是否存在
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }

        // 2. 对非空字段逐一进行部分更新（仅更新请求中携带的字段）
        if (StrUtil.isNotBlank(req.getIsbn()) && !req.getIsbn().equals(book.getIsbn())) {
            // ISBN 变更时需校验新 ISBN 的唯一性（排除当前图书自身）
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

        // 3. 持久化更新
        bookMapper.updateById(book);

        // 4. 若提供了分类 ID 列表，先清空原有关联再重新建立（替换式更新）
        if (req.getCategoryIds() != null) {
            LambdaQueryWrapper<BookCategoryRel> relWrapper = new LambdaQueryWrapper<>();
            relWrapper.eq(BookCategoryRel::getBookId, id);
            bookCategoryRelMapper.delete(relWrapper);
            saveCategoryRelations(id, req.getCategoryIds());
        }

        // 5. 重新查询最新数据并返回（确保关联信息已同步更新）
        return toBookVO(bookMapper.selectById(id));
    }

    /**
     * 删除图书。
     * <p>
     * 逻辑删除指定 ID 的图书记录（MyBatis-Plus @TableLogic 自动处理），
     * 删除后图书记录仍保留在数据库中，但查询时自动过滤。
     * </p>
     *
     * @param id 要删除的图书 ID
     * @throws BusinessException 当图书不存在时抛出 404 异常
     */
    @Override
    @SentinelResource(value = "bookDelete", fallback = "fallback", fallbackClass = SentinelRuleInitializer.class)
    @CacheEvict(value = "book", key = "#id")
    @Transactional
    public void delete(Long id) {
        // 1. 校验图书是否存在，不存在则直接抛出 404
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }
        // 2. 执行逻辑删除（MyBatis-Plus 自动将 is_deleted 设为 1）
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
        // 构造查询条件：按 ISBN 精确匹配，更新场景下排除当前图书自身
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsbn, isbn);
        if (excludeId != null) {
            wrapper.ne(Book::getId, excludeId);
        }
        // 若存在匹配记录，说明 ISBN 已被占用
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
        // 分类列表为空时无需建立关联关系
        if (CollectionUtil.isEmpty(categoryIds)) {
            return;
        }
        // 遍历分类 ID，逐条插入图书-分类关联记录
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
        // 1. 拷贝同名基础属性（书名、作者、ISBN 等）
        BookVO vo = new BookVO();
        BeanUtil.copyProperties(book, vo);
        vo.setPublishDate(book.getPublishDate());

        // 2. 查询图书关联的分类信息：通过中间表 book_category_rel 获取分类 ID 列表
        LambdaQueryWrapper<BookCategoryRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(BookCategoryRel::getBookId, book.getId());
        List<BookCategoryRel> rels = bookCategoryRelMapper.selectList(relWrapper);
        if (CollectionUtil.isNotEmpty(rels)) {
            // 有关联分类：查询分类名称列表并设置到 VO
            List<Long> categoryIds = rels.stream().map(BookCategoryRel::getCategoryId).toList();
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            vo.setCategoryNames(categories.stream().map(Category::getName).toList());
        } else {
            // 无关联分类：设置为空列表
            vo.setCategoryNames(List.of());
        }

        // 3. 查询书架名称：若有关联书架则查询其名称并设置到 VO
        if (book.getBookshelfId() != null) {
            Bookshelf bookshelf = bookshelfMapper.selectById(book.getBookshelfId());
            if (bookshelf != null) {
                vo.setBookshelfName(bookshelf.getName());
            }
        }

        return vo;
    }
}
