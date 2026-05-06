<!--
  ============================================================
  Bookshelves.vue — 管理端书架管理
  @description 管理员管理书架的 CRUD 页面，包含搜索、
               分页列表、新增/编辑对话框、删除确认功能。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="bookshelves-page">
    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书架名称"
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
        <el-button type="success" style="margin-left: auto" @click="openCreateDialog">新增书架</el-button>
      </div>
    </el-card>

    <!-- 书架列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="bookshelfList" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="书架名称" min-width="160" />
        <el-table-column prop="location" label="位置" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && bookshelfList.length === 0" description="暂无书架" />

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchBookshelves"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑书架' : '新增书架'"
      width="500px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="书架名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import type { Bookshelf, BookshelfForm } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitting = ref(false)
/** 书架列表数据 */
const bookshelfList = ref<Bookshelf[]>([])
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
/** 编辑中的书架 ID */
const editId = ref<number | null>(null)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单初始值 */
const initialForm: BookshelfForm = {
  name: '',
  location: '',
  description: ''
}

/** 书架表单数据 */
const form = reactive<BookshelfForm>({ ...initialForm })

/** 表单校验规则 */
const rules: FormRules = {
  name: [{ required: true, message: '请输入书架名称', trigger: 'blur' }]
}

/** 获取书架列表（分页 + 搜索） */
async function fetchBookshelves() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value

    const res = await api.get(AdminAPI.BOOKSHELF_PAGE.path, { params })
    const data = res.data.data
    bookshelfList.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    bookshelfList.value = []
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  page.value = 1
  fetchBookshelves()
}

/** 重置搜索条件 */
function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchBookshelves()
}

/** 打开新增对话框 */
function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, initialForm)
  dialogVisible.value = true
}

/**
 * 打开编辑对话框
 * @param bookshelf - 要编辑的书架对象
 */
function openEditDialog(bookshelf: Bookshelf) {
  isEdit.value = true
  editId.value = bookshelf.id
  form.name = bookshelf.name
  form.location = bookshelf.location || ''
  form.description = bookshelf.description || ''
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
      await api.put(AdminAPI.BOOKSHELF_UPDATE(editId.value).path, form)
      ElMessage.success('编辑成功')
    } else {
      await api.post(AdminAPI.BOOKSHELF_CREATE.path, form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchBookshelves()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

/**
 * 删除书架（带确认对话框）
 * @param bookshelf - 要删除的书架对象
 */
async function handleDelete(bookshelf: Bookshelf) {
  try {
    await ElMessageBox.confirm(`确定要删除书架「${bookshelf.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await api.delete(AdminAPI.BOOKSHELF_DELETE(bookshelf.id).path)
    ElMessage.success('删除成功')
    fetchBookshelves()
  } catch {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchBookshelves()
})
</script>

<style scoped lang="scss">
.bookshelves-page {
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
