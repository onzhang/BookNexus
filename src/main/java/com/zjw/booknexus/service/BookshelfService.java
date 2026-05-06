package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookshelfCreateReq;
import com.zjw.booknexus.dto.BookshelfPageReq;
import com.zjw.booknexus.dto.BookshelfUpdateReq;
import com.zjw.booknexus.vo.BookshelfVO;

import java.util.List;

/**
 * 书架服务接口，定义书架相关业务逻辑。
 * <p>
 * 包含书架分页搜索、详情查询、创建、更新和删除功能。
 * 实现类需处理书架名称唯一性校验等业务规则。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
public interface BookshelfService {

    /**
     * 分页查询书架。
     * <p>
     * 支持按关键词（书架名称）进行筛选，结果按创建时间倒序排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 书架分页结果
     */
    PageResult<BookshelfVO> page(BookshelfPageReq req);

    /**
     * 查询所有书架（用于下拉选择等场景）。
     *
     * @return 书架列表
     */
    List<BookshelfVO> listAll();

    /**
     * 根据 ID 查询书架详情。
     *
     * @param id 书架 ID
     * @return 书架视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当书架不存在时抛出
     */
    BookshelfVO getById(Long id);

    /**
     * 创建书架。
     * <p>
     * 校验书架名称唯一性。
     * </p>
     *
     * @param req 书架创建请求
     * @return 新创建的书架视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当书架名称已存在时抛出
     */
    BookshelfVO create(BookshelfCreateReq req);

    /**
     * 更新书架信息。
     * <p>
     * 支持部分字段更新。若更新名称，校验新名称的唯一性（排除自身）。
     * </p>
     *
     * @param id  书架 ID
     * @param req 书架更新请求
     * @return 更新后的书架视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当书架不存在时抛出
     */
    BookshelfVO update(Long id, BookshelfUpdateReq req);

    /**
     * 删除书架。
     * <p>
     * 逻辑删除指定书架记录。
     * </p>
     *
     * @param id 要删除的书架 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当书架不存在时抛出
     */
    void delete(Long id);
}
