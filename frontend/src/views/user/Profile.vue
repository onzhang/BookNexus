<!--
  ============================================================
  Profile.vue — 个人中心
  @description 展示和编辑用户个人信息，支持头像上传。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="profile-page">
    <el-card v-loading="loading" shadow="hover" class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">个人中心</span>
          <el-button v-if="!isEditing" type="primary" size="small" @click="enterEdit">
            编辑资料
          </el-button>
        </div>
      </template>

      <template v-if="!loading && userInfo">
        <!-- 头像区域 -->
        <div class="profile-header">
          <el-avatar :size="80" :src="avatarSrc" class="profile-avatar">
            {{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <div class="profile-name">
            <h2>{{ userInfo.username }}</h2>
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : ''" size="small">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>

        <div class="avatar-upload">
          <input
            type="file"
            accept="image/*"
            style="display: none"
            ref="fileInput"
            @change="handleAvatarChange"
          />
          <el-button size="small" @click="fileInput?.click()">
            上传头像
          </el-button>
        </div>

        <!-- 查看模式 -->
        <el-descriptions v-if="!isEditing" :column="1" border class="profile-info">
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo.email || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ userInfo.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="userInfo.status === 'ENABLED' ? 'success' : 'danger'" size="small">
              {{ userInfo.status === 'ENABLED' ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 编辑模式 -->
        <el-form
          v-else
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          label-position="right"
          class="profile-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </template>

      <el-empty v-if="!loading && !userInfo" description="无法获取用户信息" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'
import { UserAPI } from '@/api/endpoints'
import { useUserStore } from '@/stores/user'
import type { User } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const isEditing = ref(false)
const formRef = ref<FormInstance>()
const fileInput = ref<HTMLInputElement>()

const userInfo = ref<User | null>(userStore.userInfo)

const form = reactive({
  username: '',
  email: '',
  phone: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3-50 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

const roleText = computed(() => userInfo.value?.role === 'ADMIN' ? '管理员' : '普通用户')

/** 头像 src：兼容旧格式(完整URL)和新格式(相对路径需走代理) */
const avatarSrc = computed(() => {
  const url = userInfo.value?.avatarUrl
  if (!url) return undefined
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/api/v1/public/files/')) return url
  return '/api/v1/public/files/' + url
})

function initForm() {
  const user = userInfo.value
  if (user) {
    form.username = user.username || ''
    form.email = user.email || ''
    form.phone = user.phone || ''
  }
}

async function fetchProfile() {
  loading.value = true
  try {
    await userStore.getInfo()
    userInfo.value = userStore.userInfo
    initForm()
  } finally {
    loading.value = false
  }
}

function enterEdit() {
  initForm()
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await api.put(UserAPI.PROFILE_UPDATE.path, {
      username: form.username,
      email: form.email || undefined,
      phone: form.phone || undefined
    })
    if (res.data.data && userInfo.value) {
      Object.assign(userInfo.value, res.data.data)
      userStore.userInfo = { ...userInfo.value }
      localStorage.setItem('booknexus_user', JSON.stringify(userInfo.value))
    }
    isEditing.value = false
    ElMessage.success('资料更新成功')
  } finally {
    submitting.value = false
  }
}

async function handleAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await api.post(UserAPI.PROFILE_AVATAR.path, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const avatarUrl = res.data.data
    if (avatarUrl && userInfo.value) {
      userInfo.value.avatarUrl = avatarUrl
      userStore.userInfo = { ...userInfo.value }
      localStorage.setItem('booknexus_user', JSON.stringify(userInfo.value))
    }
    ElMessage.success('头像上传成功')
  } catch {
    // Error already shown by interceptor
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 640px;
  margin: 0 auto;

  .profile-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }

  .profile-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 8px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);

    .profile-avatar {
      background-color: var(--primary-color);
      color: var(--text-inverse);
      font-size: 32px;
      font-weight: 600;
    }

    .profile-name {
      h2 {
        font-size: 22px;
        font-weight: 700;
        color: var(--text-primary);
        margin-bottom: 4px;
      }
    }
  }

  .avatar-upload {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }

  .profile-info {
    margin-top: 0;
  }

  .profile-form {
    max-width: 480px;
    margin: 0 auto;
    padding: 0 20px;
  }
}
</style>
