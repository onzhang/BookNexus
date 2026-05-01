package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.UserPageReq;
import com.zjw.booknexus.dto.UserUpdateReq;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.UserMapper;
import com.zjw.booknexus.service.UserService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现类，实现用户管理相关的业务逻辑。
 * <p>
 * 处理用户的分页查询、详情查看、信息更新及账户状态管理。
 * 更新和状态变更操作中设有保护机制：禁止管理员禁用自身账户，
 * 防止误操作导致管理后台无法登录。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 分页查询用户列表。
     * <p>
     * 支持按关键词（用户名/邮箱）进行模糊搜索，
     * 结果按创建时间倒序排列，返回的数据不包含密码等敏感字段。
     * </p>
     *
     * @param req 分页查询参数，支持关键词搜索
     * @return 用户信息分页结果
     */
    @Override
    public PageResult<UserVO> page(UserPageReq req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.and(w -> w
                    .like(User::getUsername, req.getKeyword())
                    .or()
                    .like(User::getEmail, req.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> page = userMapper.selectPage(
                new Page<>(req.getPage(), req.getSize()), wrapper);

        List<UserVO> records = page.getRecords().stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据 ID 查询用户详情。
     * <p>
     * 查询用户实体并转换为不含敏感字段的视图对象返回。
     * </p>
     *
     * @param id 用户 ID
     * @return 用户视图对象
     * @throws BusinessException 当用户不存在时抛出 404 异常
     */
    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    /**
     * 更新用户信息。
     * <p>
     * 支持部分字段更新：邮箱、电话、状态。
     * 进行状态更新时加入保护逻辑：若当前操作的管理员试图禁用自身账户，
     * 则抛出 403 禁止操作异常，防止误操作导致账户锁定。
     * </p>
     *
     * @param id  用户 ID
     * @param req 用户更新请求，包含需要更新的字段
     * @throws BusinessException 当用户不存在时抛出 404 异常，
     *         当管理员尝试禁用自身时抛出 403 异常
     */
    @Override
    @Transactional
    public void update(Long id, UserUpdateReq req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }

        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getStatus() != null) {
            if (id.equals(UserContext.getUserId())
                    && "DISABLED".equals(req.getStatus())) {
                throw new BusinessException(403, ErrorCode.FORBIDDEN);
            }
            user.setStatus(req.getStatus());
        }

        userMapper.updateById(user);
    }

    /**
     * 启用/禁用用户账户。
     * <p>
     * 直接更新指定用户的状态字段为 ENABLED 或 DISABLED。
     * 含自身保护逻辑：禁止管理员禁用自身账户，
     * 防止误操作导致管理后台无法登录。
     * </p>
     *
     * @param id     用户 ID
     * @param status 目标状态值（ENABLED 或 DISABLED）
     * @throws BusinessException 当用户不存在时抛出 404 异常，
     *         当管理员尝试禁用自身时抛出 403 异常
     */
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }
        if (id.equals(UserContext.getUserId()) && "DISABLED".equals(status)) {
            throw new BusinessException(403, ErrorCode.FORBIDDEN);
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 将用户实体转换为视图对象。
     * <p>
     * 仅拷贝非敏感字段（排除密码），
     * 将 LocalDateTime 类型的创建时间格式化为字符串。
     * </p>
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return vo;
    }
}
