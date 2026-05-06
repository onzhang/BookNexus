package com.zjw.booknexus.service;

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
import com.zjw.booknexus.service.impl.CategoryServiceImpl;
import com.zjw.booknexus.vo.CategoryVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("分类服务单元测试")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category buildCategory(Long id, String name, Long parentId, Integer sortOrder) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder);
        return category;
    }

    @Test
    void shouldReturnPage_whenQueryWithKeyword() {
        CategoryPageReq req = new CategoryPageReq();
        req.setKeyword("Tech");

        Category category = buildCategory(1L, "Technology", 0L, 1);
        Page<Category> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(category));
        pageResult.setTotal(1);

        when(categoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        PageResult<CategoryVO> result = categoryService.page(req);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("Technology", result.getRecords().get(0).getName());
    }

    @Test
    void shouldReturnAllCategories_whenListAll() {
        Category c1 = buildCategory(1L, "A", 0L, 1);
        Category c2 = buildCategory(2L, "B", 0L, 2);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2));

        List<CategoryVO> result = categoryService.listAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnTree_whenQueryTree() {
        Category parent = buildCategory(1L, "Parent", 0L, 1);
        Category child = buildCategory(2L, "Child", 1L, 1);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(parent, child));

        List<CategoryVO> tree = categoryService.tree();

        assertEquals(1, tree.size());
        assertEquals("Parent", tree.get(0).getName());
        assertNotNull(tree.get(0).getChildren());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("Child", tree.get(0).getChildren().get(0).getName());
    }

    @Test
    void shouldReturnCategory_whenGetByIdExists() {
        Category category = buildCategory(1L, "Test", 0L, 1);
        when(categoryMapper.selectById(1L)).thenReturn(category);

        CategoryVO vo = categoryService.getById(1L);

        assertNotNull(vo);
        assertEquals("Test", vo.getName());
    }

    @Test
    void shouldThrow_whenGetByIdNotFound() {
        when(categoryMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.getById(99L));
        assertEquals(404, ex.getCode());
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void shouldCreateCategory_whenNameIsUnique() {
        CategoryCreateReq req = new CategoryCreateReq();
        req.setName("New Category");
        req.setParentId(0L);
        req.setSortOrder(1);

        when(categoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        doAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        CategoryVO vo = categoryService.create(req);

        assertNotNull(vo);
        assertEquals("New Category", vo.getName());
        assertEquals(0L, vo.getParentId());
        verify(categoryMapper).insert(any(Category.class));
    }

    @Test
    void shouldThrow_whenCreateCategoryWithDuplicateName() {
        CategoryCreateReq req = new CategoryCreateReq();
        req.setName("Existing");

        when(categoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Category());

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.create(req));
        assertEquals(409, ex.getCode());
        verify(categoryMapper, never()).insert(any(Category.class));
    }

    @Test
    void shouldUpdateCategory_whenExistsAndNameUnique() {
        Category existing = buildCategory(1L, "Old", 0L, 1);
        CategoryUpdateReq req = new CategoryUpdateReq();
        req.setName("New");
        req.setSortOrder(2);

        when(categoryMapper.selectById(1L)).thenReturn(existing);
        when(categoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);
        when(categoryMapper.selectById(1L)).thenReturn(existing);

        CategoryVO vo = categoryService.update(1L, req);

        assertNotNull(vo);
        verify(categoryMapper).updateById(any(Category.class));
    }

    @Test
    void shouldThrow_whenUpdateCategoryNotFound() {
        when(categoryMapper.selectById(99L)).thenReturn(null);

        CategoryUpdateReq req = new CategoryUpdateReq();
        req.setName("New");

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.update(99L, req));
        assertEquals(404, ex.getCode());
    }

    @Test
    void shouldThrow_whenUpdateCategoryWithSelfParent() {
        Category existing = buildCategory(1L, "Test", 0L, 1);
        CategoryUpdateReq req = new CategoryUpdateReq();
        req.setParentId(1L);

        when(categoryMapper.selectById(1L)).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.update(1L, req));
        assertEquals(400, ex.getCode());
    }

    @Test
    void shouldDeleteCategory_whenExistsAndNoChildren() {
        Category category = buildCategory(1L, "ToDelete", 0L, 1);
        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.deleteById(1L)).thenReturn(1);

        categoryService.delete(1L);

        verify(categoryMapper).deleteById(1L);
    }

    @Test
    void shouldThrow_whenDeleteCategoryNotFound() {
        when(categoryMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.delete(99L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void shouldThrow_whenDeleteCategoryWithChildren() {
        Category category = buildCategory(1L, "Parent", 0L, 1);
        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.delete(1L));
        assertEquals(409, ex.getCode());
        verify(categoryMapper, never()).deleteById(anyLong());
    }
}
