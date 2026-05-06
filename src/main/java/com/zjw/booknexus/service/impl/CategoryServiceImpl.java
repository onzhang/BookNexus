package com.zjw.booknexus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.CategoryCreateReq;
import com.zjw.booknexus.dto.CategoryPageReq;
import com.zjw.booknexus.dto.CategoryUpdateReq;
import com.zjw.booknexus.entity.Category;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.CategoryMapper;
import com.zjw.booknexus.service.CategoryService;
import com.zjw.booknexus.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现类，实现分类相关的完整业务逻辑。
 * <p>
 * 处理分类的分页搜索、详情查询、创建、更新和删除操作。
 * 支持树形结构查询和父分类关联。创建和更新操作涉及分类名称唯一性校验。
 * 使用 Hutool BeanUtil 实现属性拷贝，MyBatis-Plus 实现数据访问。
 * </p>
 *
 * @author 张俊文
 * @since 2026-05-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 分页查询分类。
     * <p>
     * 构建动态查询条件：关键词模糊匹配（分类名称），结果按排序序号升序、创建时间倒序排列。
     * 查询结果会填充父分类名称。
     * </p>
     *
     * @param req 分页查询参数
     * @return 分类分页结果
     */
    @Override
    public PageResult<CategoryVO> page(CategoryPageReq req) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getKeyword())) {
            wrapper.like(Category::getName, req.getKeyword());
        }
        wrapper.orderByAsc(Category::getSortOrder).orderByDesc(Category::getCreatedAt);

        Page<Category> mpPage = new Page<>(req.getPage(), req.getSize());
        Page<Category> result = categoryMapper.selectPage(mpPage, wrapper);

        List<CategoryVO> voList = result.getRecords().stream()
                .map(this::toCategoryVO)
                .toList();

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 查询所有分类。
     *
     * @return 分类列表
     */
    @Override
    public List<CategoryVO> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder).orderByDesc(Category::getCreatedAt);
        return categoryMapper.selectList(wrapper).stream()
                .map(this::toCategoryVO)
                .toList();
    }

    /**
     * 查询分类树形结构。
     * <p>
     * 将所有分类按 parentId 关系组装为树形结构。顶级分类的 parentId 为 0。
     * </p>
     *
     * @return 分类树列表（仅顶级分类，含 children 嵌套）
     */
    @Override
    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder).orderByDesc(Category::getCreatedAt));

        Map<Long, CategoryVO> voMap = all.stream()
                .collect(Collectors.toMap(Category::getId, this::toCategoryVO));

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : voMap.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0L) {
                roots.add(vo);
            } else {
                CategoryVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    /**
     * 根据 ID 查询分类详情。
     *
     * @param id 分类 ID
     * @return 分类视图对象
     * @throws BusinessException 当分类不存在时抛出 404 异常
     */
    @Override
    public CategoryVO getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, ErrorCode.CATEGORY_NOT_FOUND);
        }
        return toCategoryVO(category);
    }

    /**
     * 创建分类。
     * <p>
     * 校验分类名称唯一性，设置父分类关联。整个操作在一个事务中完成。
     * </p>
     *
     * @param req 分类创建请求
     * @return 新创建的分类视图对象
     * @throws BusinessException 当分类名称已存在时抛出 409 异常
     */
    @Override
    @Transactional
    public CategoryVO create(CategoryCreateReq req) {
        checkDuplicateName(req.getName(), null);

        Category category = new Category();
        BeanUtil.copyProperties(req, category);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);

        return toCategoryVO(category);
    }

    /**
     * 更新分类信息。
     * <p>
     * 对非空字段进行部分更新。若更新名称，需校验新名称的唯一性（排除当前分类自身）。
     * 不允许将分类的父分类设为其自身（避免循环依赖）。
     * </p>
     *
     * @param id  分类 ID
     * @param req 分类更新请求
     * @return 更新后的分类视图对象
     * @throws BusinessException 当分类不存在时抛出 404 异常，当名称重复时抛出 409 异常
     */
    @Override
    @Transactional
    public CategoryVO update(Long id, CategoryUpdateReq req) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, ErrorCode.CATEGORY_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(req.getName()) && !req.getName().equals(category.getName())) {
            checkDuplicateName(req.getName(), id);
            category.setName(req.getName());
        }
        if (req.getParentId() != null) {
            if (req.getParentId().equals(id)) {
                throw new BusinessException(400, "不能将分类的父分类设为其自身");
            }
            category.setParentId(req.getParentId());
        }
        if (req.getSortOrder() != null) {
            category.setSortOrder(req.getSortOrder());
        }

        categoryMapper.updateById(category);
        return toCategoryVO(categoryMapper.selectById(id));
    }

    /**
     * 删除分类。
     * <p>
     * 逻辑删除指定分类。若该分类下存在子分类，不允许删除。
     * </p>
     *
     * @param id 要删除的分类 ID
     * @throws BusinessException 当分类不存在时抛出 404 异常，当存在子分类时抛出 409 异常
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, ErrorCode.CATEGORY_NOT_FOUND);
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "该分类下存在子分类，无法删除");
        }

        categoryMapper.deleteById(id);
    }

    /**
     * 校验分类名称唯一性。
     *
     * @param name      分类名称
     * @param excludeId 需要排除的分类 ID（更新时使用），新建时传 null
     * @throws BusinessException 当名称已存在时抛出 409 异常
     */
    private void checkDuplicateName(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectOne(wrapper) != null) {
            throw new BusinessException(409, ErrorCode.DUPLICATE_CATEGORY_NAME);
        }
    }

    /**
     * 将分类实体转换为视图对象。
     * <p>
     * 使用 BeanUtil 拷贝基础属性，查询并填充父分类名称。
     * </p>
     *
     * @param category 分类实体
     * @return 分类视图对象
     */
    private CategoryVO toCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtil.copyProperties(category, vo);

        if (category.getParentId() != null && category.getParentId() != 0L) {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getName());
            }
        }

        return vo;
    }
}
