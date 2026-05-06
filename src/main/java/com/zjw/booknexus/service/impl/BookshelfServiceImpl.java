package com.zjw.booknexus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
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
import com.zjw.booknexus.service.BookshelfService;
import com.zjw.booknexus.vo.BookshelfVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 书架服务实现类，实现书架相关的完整业务逻辑。
 * <p>
 * 处理书架的分页搜索、详情查询、创建、更新和删除操作。
 * 创建和更新操作涉及书架名称唯一性校验。使用 Hutool BeanUtil 实现属性拷贝，
 * MyBatis-Plus 实现数据访问，事务注解确保数据一致性。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl implements BookshelfService {

    private final BookshelfMapper bookshelfMapper;

    /**
     * 分页查询书架。
     * <p>
     * 构建动态查询条件：关键词模糊匹配（书架名称），结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 书架分页结果
     */
    @Override
    public PageResult<BookshelfVO> page(BookshelfPageReq req) {
        LambdaQueryWrapper<Bookshelf> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getKeyword())) {
            wrapper.like(Bookshelf::getName, req.getKeyword());
        }
        wrapper.orderByDesc(Bookshelf::getCreatedAt);

        Page<Bookshelf> mpPage = new Page<>(req.getPage(), req.getSize());
        Page<Bookshelf> result = bookshelfMapper.selectPage(mpPage, wrapper);

        List<BookshelfVO> voList = result.getRecords().stream()
                .map(this::toBookshelfVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 查询所有书架。
     *
     * @return 书架列表
     */
    @Override
    public List<BookshelfVO> listAll() {
        LambdaQueryWrapper<Bookshelf> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Bookshelf::getCreatedAt);
        return bookshelfMapper.selectList(wrapper).stream()
                .map(this::toBookshelfVO)
                .toList();
    }

    /**
     * 根据 ID 查询书架详情。
     *
     * @param id 书架 ID
     * @return 书架视图对象
     * @throws BusinessException 当书架不存在时抛出 404 异常
     */
    @Override
    public BookshelfVO getById(Long id) {
        Bookshelf bookshelf = bookshelfMapper.selectById(id);
        if (bookshelf == null) {
            throw new BusinessException(404, ErrorCode.BOOKSHELF_NOT_FOUND);
        }
        return toBookshelfVO(bookshelf);
    }

    /**
     * 创建书架。
     * <p>
     * 校验书架名称唯一性，使用 BeanUtil 拷贝属性创建书架实体。
     * 整个操作在一个事务中完成。
     * </p>
     *
     * @param req 书架创建请求
     * @return 新创建的书架视图对象
     * @throws BusinessException 当书架名称已存在时抛出 409 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfVO create(BookshelfCreateReq req) {
        checkDuplicateName(req.getName(), null);

        Bookshelf bookshelf = new Bookshelf();
        BeanUtil.copyProperties(req, bookshelf);
        bookshelfMapper.insert(bookshelf);

        return toBookshelfVO(bookshelf);
    }

    /**
     * 更新书架信息。
     * <p>
     * 对非空字段进行部分更新。若更新名称，需校验新名称的唯一性（排除当前书架自身）。
     * </p>
     *
     * @param id  书架 ID
     * @param req 书架更新请求
     * @return 更新后的书架视图对象
     * @throws BusinessException 当书架不存在时抛出 404 异常，当名称重复时抛出 409 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfVO update(Long id, BookshelfUpdateReq req) {
        Bookshelf bookshelf = bookshelfMapper.selectById(id);
        if (bookshelf == null) {
            throw new BusinessException(404, ErrorCode.BOOKSHELF_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(req.getName()) && !req.getName().equals(bookshelf.getName())) {
            checkDuplicateName(req.getName(), id);
            bookshelf.setName(req.getName());
        }
        if (req.getLocation() != null) {
            bookshelf.setLocation(req.getLocation());
        }
        if (req.getDescription() != null) {
            bookshelf.setDescription(req.getDescription());
        }

        bookshelfMapper.updateById(bookshelf);
        return toBookshelfVO(bookshelfMapper.selectById(id));
    }

    /**
     * 删除书架。
     * <p>
     * 逻辑删除指定 ID 的书架记录。
     * </p>
     *
     * @param id 要删除的书架 ID
     * @throws BusinessException 当书架不存在时抛出 404 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Bookshelf bookshelf = bookshelfMapper.selectById(id);
        if (bookshelf == null) {
            throw new BusinessException(404, ErrorCode.BOOKSHELF_NOT_FOUND);
        }
        bookshelfMapper.deleteById(id);
    }

    /**
     * 校验书架名称唯一性。
     *
     * @param name      书架名称
     * @param excludeId 需要排除的书架 ID（更新时使用），新建时传 null
     * @throws BusinessException 当名称已存在时抛出 409 异常
     */
    private void checkDuplicateName(String name, Long excludeId) {
        LambdaQueryWrapper<Bookshelf> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookshelf::getName, name);
        if (excludeId != null) {
            wrapper.ne(Bookshelf::getId, excludeId);
        }
        if (bookshelfMapper.selectOne(wrapper) != null) {
            throw new BusinessException(409, ErrorCode.DUPLICATE_BOOKSHELF_NAME);
        }
    }

    /**
     * 将书架实体转换为视图对象。
     *
     * @param bookshelf 书架实体
     * @return 书架视图对象
     */
    private BookshelfVO toBookshelfVO(Bookshelf bookshelf) {
        BookshelfVO vo = new BookshelfVO();
        BeanUtil.copyProperties(bookshelf, vo);
        return vo;
    }
}
