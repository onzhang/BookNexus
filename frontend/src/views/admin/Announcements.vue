<!--
  ============================================================
  Announcements.vue — 管理端公告管理
  @description 管理员管理公告的 CRUD 页面，包含搜索、
               分页列表、新增/编辑对话框、删除确认功能。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="announcements-page">
    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索公告标题"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" style="margin-left: auto" @click="openCreateDialog">
          新增公告
        </el-button>
      </div>
    </el-card>

    <!-- 公告列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="list" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPublished === 1 ? 'success' : 'info'">
              {{ row.isPublished === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无公告" />

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '新增公告'"
      width="600px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="form.isPublished">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
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
import type { Announcement, AnnouncementForm } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitting = ref(false)
/** 公告列表数据 */
const list = ref<Announcement[]>([])
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
/** 编辑中的公告 ID */
const editId = ref<number | null>(null)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单初始值 */
const initialForm: AnnouncementForm = {
  title: '',
  content: '',
  isPublished: 0
}

/** 公告表单数据 */
const form = reactive<AnnouncementForm>({ ...initialForm })

/** 表单校验规则 */
const rules: FormRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

/** 获取公告列表（分页 + 搜索） */
async function fetchList() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value

    const res = await api.get(AdminAPI.ANNOUNCEMENT_PAGE.path, { params })
    const data = res.data.data
    list.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  page.value = 1
  fetchList()
}

/** 重置搜索条件 */
function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchList()
}

/** 打开新增对话框 */
function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, initialForm)
  dialogVisible.value = true
}

/** 打开编辑对话框 */
function openEditDialog(row: Announcement) {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.content = row.content
  form.isPublished = row.isPublished
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
      await api.put(AdminAPI.ANNOUNCEMENT_UPDATE(editId.value).path, form)
      ElMessage.success('编辑成功')
    } else {
      await api.post(AdminAPI.ANNOUNCEMENT_CREATE.path, form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

/** 删除公告（带确认对话框） */
async function handleDelete(row: Announcement) {
  try {
    await ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await api.delete(AdminAPI.ANNOUNCEMENT_DELETE(row.id).path)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.announcements-page {
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
