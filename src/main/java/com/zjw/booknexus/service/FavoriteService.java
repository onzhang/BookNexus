package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.FavoriteReq;
import com.zjw.booknexus.vo.FavoriteVO;

/**
 * 收藏服务接口，定义用户收藏相关的核心业务逻辑。
 * <p>
 * 包含用户端的收藏图书、取消收藏及收藏列表查询功能。
 * 实现类需处理重复收藏校验、图书存在性校验等业务逻辑。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
public interface FavoriteService {

    /**
     * 收藏图书。
     * <p>
     * 校验图书是否存在，且用户尚未收藏该书。
     * 通过校验后创建收藏记录。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    收藏请求，包含图书 ID
     * @return 收藏记录视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当图书不存在时抛出 404 异常，
     *         当已收藏该书时抛出 409 异常
     */
    FavoriteVO addFavorite(Long userId, FavoriteReq req);

    /**
     * 取消收藏。
     * <p>
     * 根据当前用户 ID 和图书 ID 删除对应的收藏记录。
     * 仅允许删除当前用户自己的收藏记录。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当收藏记录不存在时抛出 404 异常
     */
    void removeFavorite(Long userId, Long bookId);

    /**
     * 查询当前用户的收藏列表。
     * <p>
     * 分页查询指定用户的收藏记录，结果按收藏时间倒序排列。
     * 每条记录附带对应的图书信息。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param page   当前页码
     * @param size   每页大小
     * @return 收藏记录分页结果
     */
    PageResult<FavoriteVO> myFavorites(Long userId, Integer page, Integer size);

    /**
     * 检查用户是否已收藏指定图书。
     *
     * @param userId 当前用户 ID
     * @param bookId 图书 ID
     * @return true=已收藏，false=未收藏
     */
    boolean isFavorited(Long userId, Long bookId);
}
