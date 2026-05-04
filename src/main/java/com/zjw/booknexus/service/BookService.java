package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookCreateReq;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.dto.BookUpdateReq;
import com.zjw.booknexus.vo.BookVO;

/**
 * 图书服务接口，定义图书相关业务逻辑。
 * <p>
 * 包含图书分页搜索、详情查询、创建、更新和删除功能。
 * 实现类需处理分类关联、书架关联及 ISBN 唯一性校验等业务规则。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
public interface BookService {

    /**
     * 分页查询图书。
     * <p>
     * 支持按关键词（书名、作者、ISBN）、图书状态和书架 ID 进行筛选，
     * 结果按创建时间倒序排列。返回的图书信息包含关联的分类名称和书架名称。
     * </p>
     *
     * @param req 分页查询参数
     * @return 图书分页结果
     */
    PageResult<BookVO> page(BookPageReq req);

    /**
     * 根据 ID 查询图书详情。
     * <p>
     * 返回图书的完整信息，包括关联的分类名称列表和书架名称。
     * </p>
     *
     * @param id 图书 ID
     * @return 图书视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当图书不存在时抛出
     */
    BookVO getById(Long id);

    /**
     * 创建图书。
     * <p>
     * 校验 ISBN 唯一性，设置图书初始状态为 AVAILABLE，并建立图书与分类的关联关系。
     * </p>
     *
     * @param req 图书创建请求
     * @return 新创建的图书视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当 ISBN 已存在时抛出
     */
    BookVO create(BookCreateReq req);

    /**
     * 更新图书信息。
     * <p>
     * 支持部分字段更新。若更新 ISBN，校验新 ISBN 的唯一性（排除自身）。
     * 若提供分类 ID 列表，则重建图书与分类的关联关系。
     * </p>
     *
     * @param id  图书 ID
     * @param req 图书更新请求
     * @return 更新后的图书视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当图书不存在时抛出
     */
    BookVO update(Long id, BookUpdateReq req);

    /**
     * 删除图书。
     * <p>
     * 根据 ID 物理删除图书记录，操作不可恢复。
     * </p>
     *
     * @param id 要删除的图书 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当图书不存在时抛出
     */
    void delete(Long id);
}
