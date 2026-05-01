package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.AdminBorrowPageReq;
import com.zjw.booknexus.dto.BorrowPageReq;
import com.zjw.booknexus.dto.BorrowReq;
import com.zjw.booknexus.vo.BorrowRecordVO;

/**
 * 借阅服务接口，定义图书借阅相关的核心业务逻辑。
 * <p>
 * 包含用户端的借书、还书、续借、借阅记录查询，
 * 以及管理端的借阅记录分页查询和强制归还功能。
 * 实现类需处理借阅规则校验、库存状态更新、逾期罚金计算等业务逻辑。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface BorrowService {

    /**
     * 用户借阅图书。
     * <p>
     * 校验图书是否存在且可借阅、用户当前借阅数量是否未超上限（5 本）、
     * 用户是否未借阅该书。通过校验后创建借阅记录并更新图书状态为 BORROWED。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    借阅请求，包含图书 ID
     * @return 借阅记录视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当图书不存在、不可借阅、
     *         借阅数量超限或重复借阅时抛出
     */
    BorrowRecordVO borrow(Long userId, BorrowReq req);

    /**
     * 用户归还图书。
     * <p>
     * 根据当前用户 ID 和借阅记录 ID 查询并更新借阅记录为已归还状态。
     * 若归还日期超过应还日期，按 0.10 元/天计算逾期罚金。
     * 归还后更新图书状态为 AVAILABLE。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param recordId 借阅记录 ID
     * @return 更新后的借阅记录视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当记录不存在或状态不合法时抛出
     */
    BorrowRecordVO returnBook(Long userId, Long recordId);

    /**
     * 用户续借图书。
     * <p>
     * 对指定借阅记录进行续借操作，续借后应还日期延长 15 天。
     * 每本书最多续借 1 次，超出限制不可再续借。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param recordId 借阅记录 ID
     * @return 更新后的借阅记录视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当记录不存在、状态不合法
     *         或续借次数已达上限时抛出
     */
    BorrowRecordVO renew(Long userId, Long recordId);

    /**
     * 查询当前用户的借阅记录。
     * <p>
     * 分页查询指定用户的借阅记录，可按借阅状态（BORROWED/RENEWED/RETURNED）筛选，
     * 结果按创建时间倒序排列。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    分页查询参数
     * @return 借阅记录分页结果
     */
    PageResult<BorrowRecordVO> myBorrows(Long userId, BorrowPageReq req);

    /**
     * 管理员分页查询所有借阅记录。
     * <p>
     * 支持按借阅状态、用户 ID 和关键词（用户名/图书名）进行筛选，
     * 关键词搜索时同时匹配用户表和图书表，结果按创建时间倒序排列。
     * </p>
     *
     * @param req 管理员借阅分页查询参数
     * @return 所有借阅记录的分页结果
     */
    PageResult<BorrowRecordVO> adminPage(AdminBorrowPageReq req);

    /**
     * 管理员强制归还图书。
     * <p>
     * 管理员可绕过用户身份校验直接强制归还指定借阅记录的图书，
     * 适用于线下还书等管理场景。归还后自动计算逾期罚金。
     * </p>
     *
     * @param recordId 借阅记录 ID
     * @return 更新后的借阅记录视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当记录不存在或状态不合法时抛出
     */
    BorrowRecordVO adminReturnRecord(Long recordId);
}
