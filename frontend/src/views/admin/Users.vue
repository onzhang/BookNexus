<template>
  <div class="users-page">
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
import type { User } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const userList = ref<User[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const dialogVisible = ref(false)
const editUser = ref<User | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  email: '',
  phone: ''
})

const rules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

async function fetchUsers() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value

    const res = await api.get('/v1/admin/users', { params })
    const data = res.data.data
    userList.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    userList.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchUsers()
}

function resetSearch() {
  keyword.value = ''
  page.value = 1
  fetchUsers()
}

function openEditDialog(user: User) {
  editUser.value = user
  form.email = user.email || ''
  form.phone = user.phone || ''
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  editUser.value = null
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !editUser.value) return

  submitting.value = true
  try {
    await api.put(`/v1/admin/users/${editUser.value.id}`, {
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

async function handleStatusChange(user: User, enabled: boolean) {
  try {
    await api.put(`/v1/admin/users/${user.id}`, {
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
