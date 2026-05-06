package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.FavoriteReq;
import com.zjw.booknexus.entity.Book;
import com.zjw.booknexus.entity.Favorite;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.BookMapper;
import com.zjw.booknexus.mapper.FavoriteMapper;
import com.zjw.booknexus.service.FavoriteService;
import com.zjw.booknexus.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏服务实现类，实现用户收藏相关的完整业务逻辑。
 * <p>
 * 处理用户收藏图书、取消收藏及收藏列表查询。
 * 核心业务规则包括：同一用户不能重复收藏同一本书、取消收藏时仅删除当前用户的记录。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;

    /**
     * 收藏图书。
     * <p>
     * 执行完整的收藏前校验：图书是否存在、用户是否已收藏该书。
     * 校验通过后创建收藏记录，并记录当前时间为收藏时间。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    收藏请求，包含图书 ID
     * @return 收藏记录视图对象，包含图书名、作者等关联信息
     * @throws BusinessException 当图书不存在时抛出 404 异常，
     *         当已收藏该书时抛出 409 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteVO addFavorite(Long userId, FavoriteReq req) {
        // 1. 校验图书是否存在
        Book book = bookMapper.selectById(req.getBookId());
        if (book == null) {
            throw new BusinessException(404, ErrorCode.BOOK_NOT_FOUND);
        }

        // 2. 校验用户是否已收藏该书
        long existingCount = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getBookId, req.getBookId()));
        if (existingCount > 0) {
            throw new BusinessException(409, ErrorCode.ALREADY_FAVORITED);
        }

        // 3. 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookId(req.getBookId());
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);

        // 4. 组装视图对象返回
        return buildVO(favorite, book);
    }

    /**
     * 取消收藏。
     * <p>
     * 根据当前用户 ID 和图书 ID 精确查询并删除收藏记录，
     * 确保仅删除当前用户自己的收藏。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @throws BusinessException 当收藏记录不存在时抛出 404 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long bookId) {
        // 按用户 ID 和图书 ID 精确查询收藏记录
        Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getBookId, bookId));
        if (favorite == null) {
            throw new BusinessException(404, ErrorCode.FAVORITE_NOT_FOUND);
        }

        favoriteMapper.deleteById(favorite.getId());
    }

    /**
     * 查询当前用户的收藏列表。
     * <p>
     * 分页查询指定用户的收藏记录，按收藏时间倒序排列。
     * 每条记录查询对应的图书信息并组装为视图对象。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param page   当前页码
     * @param size   每页大小
     * @return 收藏记录分页结果
     */
    @Override
    public PageResult<FavoriteVO> myFavorites(Long userId, Integer page, Integer size) {
        // 1. 构建查询条件：按当前用户 ID 精确过滤，按收藏时间倒序
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);

        // 2. 执行分页查询
        Page<Favorite> pageParam = new Page<>(page, size);
        IPage<Favorite> result = favoriteMapper.selectPage(pageParam, wrapper);

        // 3. 流式转换为 VO：每条记录查询对应的图书信息
        List<FavoriteVO> vos = result.getRecords().stream()
                .map(favorite -> {
                    Book book = bookMapper.selectById(favorite.getBookId());
                    return buildVO(favorite, book);
                }).toList();

        // 4. 组装分页结果返回
        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 检查用户是否已收藏指定图书。
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @return true=已收藏，false=未收藏
     */
    @Override
    public boolean isFavorited(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getBookId, bookId)) > 0;
    }

    /**
     * 构建收藏记录视图对象。
     * <p>
     * 将收藏记录实体与关联的图书信息组装为视图对象。
     * 图书可能为空（如已被删除），此时对应的名称字段置为 null。
     * </p>
     *
     * @param favorite 收藏记录实体
     * @param book     关联的图书实体（可能为 null）
     * @return 收藏记录视图对象
     */
    private FavoriteVO buildVO(Favorite favorite, Book book) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(favorite.getId());
        vo.setUserId(favorite.getUserId());
        vo.setBookId(favorite.getBookId());
        vo.setBookTitle(book != null ? book.getTitle() : null);
        vo.setBookAuthor(book != null ? book.getAuthor() : null);
        vo.setBookCoverUrl(book != null ? book.getCoverUrl() : null);
        vo.setBookStatus(book != null ? book.getStatus() : null);
        vo.setCreatedAt(favorite.getCreatedAt());
        return vo;
    }
}
