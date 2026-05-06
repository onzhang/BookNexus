<!--
  ============================================================
  Books.vue — 管理端书籍管理
  @description 管理员管理书籍的 CRUD 页面，包含搜索/状态筛选、
               分页列表、新增/编辑对话框、删除确认功能。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="books-page">
    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书名或作者"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 160px">
          <el-option label="可借" value="AVAILABLE" />
          <el-option label="已借出" value="BORROWED" />
          <el-option label="损坏" value="DAMAGED" />
          <el-option label="丢失" value="LOST" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" style="margin-left: auto" @click="openCreateDialog">新增书籍</el-button>
      </div>
    </el-card>

    <!-- 书籍列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="bookList" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="160" />
        <el-table-column prop="isbn" label="ISBN" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="80">
          <template #default="{ row }">
            {{ row.availableStock }}/{{ row.stock }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && bookList.length === 0" description="暂无书籍" />

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchBooks"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑书籍' : '新增书籍'"
      width="600px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="form.publisher" />
        </el-form-item>
        <el-form-item label="分类ID">
          <el-input-number v-model="form.categoryId" :min="0" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="form.coverUrl" />
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
import type { Book, BookForm } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitting = ref(false)
/** 书籍列表数据 */
const bookList = ref<Book[]>([])
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)
/** 搜索关键字 */
const keyword = ref('')
/** 状态筛选条件 */
const statusFilter = ref('')

/** 对话框是否可见 */
const dialogVisible = ref(false)
/** 是否为编辑模式 */
const isEdit = ref(false)
/** 编辑中的书籍 ID */
const editId = ref<number | null>(null)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单初始值 */
const initialForm: BookForm = {
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  categoryId: undefined,
  stock: 1,
  description: '',
  coverUrl: ''
}

/** 书籍表单数据 */
const form = reactive<BookForm>({ ...initialForm })

/** 表单校验规则 */
const rules: FormRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

/** 书籍状态 → 中文文本映射 */
const statusTextMap: Record<string, string> = {
  AVAILABLE: '可借',
  BORROWED: '已借出',
  DAMAGED: '损坏',
  LOST: '丢失'
}

/** 书籍状态 → Element Plus Tag 类型映射 */
const statusTagMap: Record<string, string> = {
  AVAILABLE: 'success',
  BORROWED: 'warning',
  DAMAGED: 'danger',
  LOST: 'info'
}

/**
 * 根据状态码返回中文文本
 * @param status - 状态码
 */
function statusText(status: string) {
  return statusTextMap[status] || status
}

/**
 * 根据状态码返回标签类型
 * @param status - 状态码
 */
function statusTagType(status: string) {
  return statusTagMap[status] || 'info'
}

/** 获取书籍列表（分页 + 搜索 + 筛选） */
async function fetchBooks() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value) params.status = statusFilter.value

    const res = await api.get(AdminAPI.BOOK_PAGE.path, { params })
    const data = res.data.data
    bookList.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    bookList.value = []
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  page.value = 1
  fetchBooks()
}

/** 重置搜索条件 */
function resetSearch() {
  keyword.value = ''
  statusFilter.value = ''
  page.value = 1
  fetchBooks()
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
 * @param book - 要编辑的书籍对象
 */
function openEditDialog(book: Book) {
  isEdit.value = true
  editId.value = book.id
  form.title = book.title
  form.author = book.author
  form.isbn = book.isbn
  form.publisher = book.publisher || ''
  form.categoryId = book.categoryId
  form.stock = book.stock
  form.description = book.description || ''
  form.coverUrl = book.coverUrl || ''
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
      await api.put(AdminAPI.BOOK_UPDATE(editId.value).path, form)
      ElMessage.success('编辑成功')
    } else {
      await api.post(AdminAPI.BOOK_CREATE.path, form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

/**
 * 删除书籍（带确认对话框）
 * @param book - 要删除的书籍对象
 */
async function handleDelete(book: Book) {
  try {
    await ElMessageBox.confirm(`确定要删除书籍「${book.title}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await api.delete(AdminAPI.BOOK_DELETE(book.id).path)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped lang="scss">
.books-page {
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
