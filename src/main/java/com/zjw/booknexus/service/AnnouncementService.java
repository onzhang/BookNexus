package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.AnnouncementCreateReq;
import com.zjw.booknexus.dto.AnnouncementPageReq;
import com.zjw.booknexus.dto.AnnouncementUpdateReq;
import com.zjw.booknexus.vo.AnnouncementVO;

/**
 * 公告服务接口，定义公告相关业务逻辑。
 * <p>
 * 包含公告的分页查询、详情查询、创建、更新和删除功能。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface AnnouncementService {

    /**
     * 分页查询公告。
     * <p>
     * 支持按关键字（标题）进行模糊搜索，结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 公告分页结果
     */
    PageResult<AnnouncementVO> page(AnnouncementPageReq req);

    /**
     * 根据 ID 查询公告详情。
     *
     * @param id 公告 ID
     * @return 公告视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当公告不存在时抛出
     */
    AnnouncementVO getById(Long id);

    /**
     * 创建公告。
     * <p>
     * 自动将当前登录用户设为发布人。
     * </p>
     *
     * @param req 公告创建请求
     * @return 新创建的公告视图对象
     */
    AnnouncementVO create(AnnouncementCreateReq req);

    /**
     * 更新公告信息。
     * <p>
     * 支持部分字段更新。
     * </p>
     *
     * @param id  公告 ID
     * @param req 公告更新请求
     * @return 更新后的公告视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当公告不存在时抛出
     */
    AnnouncementVO update(Long id, AnnouncementUpdateReq req);

    /**
     * 删除公告。
     * <p>
     * 逻辑删除指定 ID 的公告记录。
     * </p>
     *
     * @param id 要删除的公告 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当公告不存在时抛出
     */
    void delete(Long id);
}
