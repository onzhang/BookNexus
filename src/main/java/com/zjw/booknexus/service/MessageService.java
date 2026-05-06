package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.MessageCreateReq;
import com.zjw.booknexus.dto.MessageReplyReq;
import com.zjw.booknexus.vo.MessageVO;

/**
 * 留言服务接口，定义留言相关业务逻辑。
 * <p>
 * 包含用户提交留言、查询我的留言列表、管理员查看所有留言及回复留言功能。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface MessageService {

    /**
     * 分页查询当前用户的留言列表。
     * <p>
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param page 当前页码
     * @param size 每页大小
     * @param userId 当前用户 ID
     * @return 留言分页结果
     */
    PageResult<MessageVO> pageByUser(int page, int size, Long userId);

    /**
     * 分页查询所有留言（管理员使用）。
     * <p>
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param page 当前页码
     * @param size 每页大小
     * @return 留言分页结果
     */
    PageResult<MessageVO> pageAll(int page, int size);

    /**
     * 创建留言。
     *
     * @param req    留言创建请求
     * @param userId 当前用户 ID
     * @return 新创建的留言视图对象
     */
    MessageVO create(MessageCreateReq req, Long userId);

    /**
     * 回复留言。
     *
     * @param id       留言 ID
     * @param req      回复请求
     * @param replierId 回复管理员 ID
     * @return 更新后的留言视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当留言不存在时抛出
     */
    MessageVO reply(Long id, MessageReplyReq req, Long replierId);
}
