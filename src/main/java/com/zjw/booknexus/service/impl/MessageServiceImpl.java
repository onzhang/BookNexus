package com.zjw.booknexus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.MessageCreateReq;
import com.zjw.booknexus.dto.MessageReplyReq;
import com.zjw.booknexus.entity.Message;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.MessageMapper;
import com.zjw.booknexus.service.MessageService;
import com.zjw.booknexus.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言服务实现类，实现留言相关的业务逻辑。
 * <p>
 * 处理用户提交留言、查询留言列表及管理员回复留言操作。
 * 使用 Hutool BeanUtil 实现属性拷贝，MyBatis-Plus 实现数据访问。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    /**
     * 分页查询当前用户的留言列表。
     * <p>
     * 按用户 ID 精确匹配，结果按创建时间倒序排列。
     * </p>
     *
     * @param page   当前页码
     * @param size   每页大小
     * @param userId 当前用户 ID
     * @return 留言分页结果
     */
    @Override
    public PageResult<MessageVO> pageByUser(int page, int size, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> mpPage = new Page<>(page, size);
        Page<Message> result = messageMapper.selectPage(mpPage, wrapper);

        List<MessageVO> voList = result.getRecords().stream()
                .map(this::toMessageVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

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
    @Override
    public PageResult<MessageVO> pageAll(int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> mpPage = new Page<>(page, size);
        Page<Message> result = messageMapper.selectPage(mpPage, wrapper);

        List<MessageVO> voList = result.getRecords().stream()
                .map(this::toMessageVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建留言。
     *
     * @param req    留言创建请求
     * @param userId 当前用户 ID
     * @return 新创建的留言视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO create(MessageCreateReq req, Long userId) {
        Message message = new Message();
        BeanUtil.copyProperties(req, message);
        message.setUserId(userId);

        messageMapper.insert(message);
        return toMessageVO(message);
    }

    /**
     * 回复留言。
     * <p>
     * 校验留言是否存在，然后写入回复内容、回复时间和回复人 ID。
     * </p>
     *
     * @param id        留言 ID
     * @param req       回复请求
     * @param replierId 回复管理员 ID
     * @return 更新后的留言视图对象
     * @throws BusinessException 当留言不存在时抛出 404 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO reply(Long id, MessageReplyReq req, Long replierId) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, ErrorCode.MESSAGE_NOT_FOUND);
        }

        message.setReply(req.getReply());
        message.setReplyAt(LocalDateTime.now());
        message.setReplierId(replierId);

        messageMapper.updateById(message);
        return toMessageVO(messageMapper.selectById(id));
    }

    /**
     * 将留言实体转换为视图对象。
     *
     * @param message 留言实体
     * @return 留言视图对象
     */
    private MessageVO toMessageVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtil.copyProperties(message, vo);
        return vo;
    }
}
