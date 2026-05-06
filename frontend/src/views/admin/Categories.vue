<!--
  ============================================================
  Categories.vue — 管理端分类管理
  @description 管理员管理分类的 CRUD 页面，包含搜索、
               分页列表、树形展示、新增/编辑对话框、删除确认功能。
               支持多级树形结构（parent_id 自关联）。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="categories-page">
    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索分类名称"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" style="margin-left: auto" @click="openCreateDialog">新增分类</el-button>
      </div>
    </el-card>

    <!-- 分类列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table
        :data="categoryList"
        stripe
        border
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        default-expand-all
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="parentName" label="父分类" width="140">
          <template #default="{ row }">
            {{ row.parentName || (row.parentId === 0 ? '顶级分类' : '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && categoryList.length === 0" description="暂无分类" />

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchCategories"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            :props="treeProps"
            check-strictly
            clearable
            placeholder="请选择父分类（不选则为顶级分类）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序序号">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import { AdminAPI } from '@/api/endpoints'
import type { Category } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

interface CategoryForm {
  name: string
  parentId: number
  sortOrder: number
}

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitting = ref(false)
/** 分类列表数据 */
const categoryList = ref<Category[]>([])
/** 分类树数据（用于下拉选择） */
const categoryTree = ref<Category[]>([])
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)
/** 搜索关键字 */
const keyword = ref('')

/** 对话框是否可见 */
const dialogVisible = ref(false)
/** 是否为编辑模式 */
const isEdit = ref(false)
/** 编辑中的分类 ID */
const editId = ref<number | null>(null)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单初始值 */
const initialForm: CategoryForm = {
  name: '',
  parentId: 0,
  sortOrder: 0
}

/** 分类表单数据 */
const form = reactive<CategoryForm>({ ...initialForm })

/** 树形选择器配置 */
const treeProps = {
  label: 'name',
  value: 'id',
  children: 'children'
}

/** 表单校验规则 */
const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

/** 获取分类列表（分页 + 搜索） */
async function fetchCategories() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value

    const res = await api.get(AdminAPI.CATEGORY_PAGE.path, { params })
    const data = res.data.data
    categoryList.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    categoryList.value = []
  } finally {
    loading.value = false
  }
}

/** 获取分类树 */
async function fetchCategoryTree() {
  try {
    const res = await api.get(AdminAPI.CATEGORY_TREE_ADMIN.path)
    categoryTree.value = res.data.data ?? []
  } catch {
    categoryTree.value = []
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  page.value = 1
  fetchCategories()
}

/** 重置搜索条件 */
function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchCategories()
}

/** 打开新增对话框 */
function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, initialForm)
  dialogVisible.value = true
}

/** 打开编辑对话框 */
function openEditDialog(category: Category) {
  isEdit.value = true
  editId.value = category.id
  form.name = category.name
  form.parentId = category.parentId
  form.sortOrder = category.sortOrder
  dialogVisible.value = true
}

/** 关闭对话框时重置表单 */
function resetForm() {
  formRef.value?.resetFields()
}

/** 提交新增或编辑 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await api.put(AdminAPI.CATEGORY_UPDATE(editId.value).path, form)
      ElMessage.success('编辑成功')
    } else {
      await api.post(AdminAPI.CATEGORY_CREATE.path, form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchCategories()
    fetchCategoryTree()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

/** 删除分类（带确认对话框） */
async function handleDelete(category: Category) {
  try {
    await ElMessageBox.confirm(`确定要删除分类「${category.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await api.delete(AdminAPI.CATEGORY_DELETE(category.id).path)
    ElMessage.success('删除成功')
    fetchCategories()
    fetchCategoryTree()
  } catch {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchCategories()
  fetchCategoryTree()
})
</script>

<style scoped lang="scss">
.categories-page {
  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .table-card {
    margin-top: 20px;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
