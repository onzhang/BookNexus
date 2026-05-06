package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.CategoryCreateReq;
import com.zjw.booknexus.dto.CategoryPageReq;
import com.zjw.booknexus.dto.CategoryUpdateReq;
import com.zjw.booknexus.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口，定义分类相关业务逻辑。
 * <p>
 * 包含分类分页搜索、详情查询、创建、更新和删除功能。
 * 支持树形结构查询和父分类关联。实现类需处理分类名称唯一性校验等业务规则。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
public interface CategoryService {

    /**
     * 分页查询分类。
     * <p>
     * 支持按关键词（分类名称）进行筛选，结果按排序序号和创建时间排列。
     * </p>
     *
     * @param req 分页查询参数
     * @return 分类分页结果
     */
    PageResult<CategoryVO> page(CategoryPageReq req);

    /**
     * 查询所有分类（用于下拉选择等场景）。
     *
     * @return 分类列表
     */
    List<CategoryVO> listAll();

    /**
     * 查询分类树形结构。
     * <p>
     * 将所有分类按 parentId 关系组装为树形结构返回，便于前端展示层级关系。
     * </p>
     *
     * @return 分类树列表（仅顶级分类，含 children 嵌套）
     */
    List<CategoryVO> tree();

    /**
     * 根据 ID 查询分类详情。
     *
     * @param id 分类 ID
     * @return 分类视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当分类不存在时抛出
     */
    CategoryVO getById(Long id);

    /**
     * 创建分类。
     * <p>
     * 校验分类名称唯一性，设置父分类关联。
     * </p>
     *
     * @param req 分类创建请求
     * @return 新创建的分类视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当分类名称已存在时抛出
     */
    CategoryVO create(CategoryCreateReq req);

    /**
     * 更新分类信息。
     * <p>
     * 支持部分字段更新。若更新名称，校验新名称的唯一性（排除自身）。
     * 不允许将分类的父分类设为其自身或其子分类（避免循环依赖）。
     * </p>
     *
     * @param id  分类 ID
     * @param req 分类更新请求
     * @return 更新后的分类视图对象
     * @throws com.zjw.booknexus.exception.BusinessException 当分类不存在时抛出
     */
    CategoryVO update(Long id, CategoryUpdateReq req);

    /**
     * 删除分类。
     * <p>
     * 逻辑删除指定分类记录。若该分类下存在子分类，不允许删除。
     * </p>
     *
     * @param id 要删除的分类 ID
     * @throws com.zjw.booknexus.exception.BusinessException 当分类不存在或存在子分类时抛出
     */
    void delete(Long id);
}
