package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.UserPageReq;
import com.zjw.booknexus.dto.UserUpdateReq;
import com.zjw.booknexus.vo.UserVO;

/**
 * 用户服务接口，定义用户管理相关的业务逻辑。
 * <p>
 * 包含用户分页查询、详情查看、信息更新和账户状态管理功能。
 * 实现类需处理权限校验（如禁止管理员禁用自身）等业务规则。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface UserService {

    /**
     * 分页查询用户列表。
     * <p>
     * 支持按关键词（用户名/邮箱）进行模糊搜索，
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 用户信息分页结果
     */
    PageResult<UserVO> page(UserPageReq req);

    /**
     * 根据 ID 查询用户详情。
     * <p>
     * 返回用户的完整信息，不包含密码等敏感字段。
     * </p>
     *
     * @param id 用户 ID
     * @return 用户视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当用户不存在时抛出
     */
    UserVO getById(Long id);

    /**
     * 更新用户信息。
     * <p>
     * 支持部分字段更新（邮箱、电话、状态）。
     * 若尝试禁用当前登录用户自身，则抛出禁止操作异常。
     * </p>
     *
     * @param id  用户 ID
     * @param req 用户更新请求
     * @throws com.zjw.booknexus.exception.BusinessException 当用户不存在或
     *         尝试禁用自身时抛出
     */
    void update(Long id, UserUpdateReq req);

    /**
     * 启用/禁用用户账户。
     * <p>
     * 直接更新用户的状态字段（ENABLED/DISABLED）。
     * 不允许管理员禁用自身账户。
     * </p>
     *
     * @param id     用户 ID
     * @param status 目标状态（ENABLED 或 DISABLED）
     * @throws com.zjw.booknexus.exception.BusinessException 当用户不存在或
     *         尝试禁用自身时抛出
     */
    void updateStatus(Long id, String status);
}
