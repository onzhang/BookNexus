<!--
  ============================================================
  Users.vue — 管理端用户管理
  @description 管理员管理用户的页面，支持搜索、分页列表、编辑邮箱/手机号、
               启用/禁用状态切换。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="users-page">
    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名或邮箱"
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
      </div>
    </el-card>

    <!-- 用户列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="userList" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'ENABLED'"
              active-text="启用"
              inactive-text="禁用"
              @change="(val: boolean) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && userList.length === 0" description="暂无用户" />

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑用户"
      width="500px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="editUser?.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { AdminAPI } from '@/api/endpoints'
import type { User } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitting = ref(false)
/** 用户列表数据 */
const userList = ref<User[]>([])
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
/** 当前编辑的用户 */
const editUser = ref<User | null>(null)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 编辑表单数据 */
const form = reactive({
  email: '',
  phone: ''
})

/** 表单校验规则（邮箱格式验证） */
const rules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

/** 获取用户列表（分页 + 搜索） */
async function fetchUsers() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value

    const res = await api.get(AdminAPI.USER_PAGE.path, { params })
    const data = res.data.data
    userList.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    userList.value = []
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  page.value = 1
  fetchUsers()
}

/** 重置搜索条件 */
function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchUsers()
}

/**
 * 打开编辑对话框
 * @param user - 要编辑的用户对象
 */
function openEditDialog(user: User) {
  editUser.value = user
  form.email = user.email || ''
  form.phone = user.phone || ''
  dialogVisible.value = true
}

/** 关闭对话框后重置表单 */
function resetForm() {
  formRef.value?.resetFields()
  editUser.value = null
}

/** 提交用户编辑 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !editUser.value) return

  submitting.value = true
  try {
    await api.put(AdminAPI.USER_UPDATE(editUser.value.id).path, {
      email: form.email,
      phone: form.phone
    })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchUsers()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

/**
 * 切换用户启用/禁用状态
 * @param user - 目标用户
 * @param enabled - 是否启用
 */
async function handleStatusChange(user: User, enabled: boolean) {
  try {
    await api.put(AdminAPI.USER_STATUS(user.id).path, {
      status: enabled ? 'ENABLED' : 'DISABLED'
    })
    user.status = enabled ? 'ENABLED' : 'DISABLED'
    ElMessage.success(enabled ? '用户已启用' : '用户已禁用')
  } catch {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped lang="scss">
.users-page {
  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
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
