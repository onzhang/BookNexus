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
        <span class="card-title">个人中心</span>
      </template>

      <template v-if="!loading && userInfo">
        <!-- 用户头像和姓名 -->
        <div class="profile-header">
          <el-avatar :size="80" :src="userInfo.avatarUrl || undefined" class="profile-avatar">
            {{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <div class="profile-name">
            <h2>{{ userInfo.username }}</h2>
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : ''" size="small">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>

        <!-- 头像上传 -->
        <div class="avatar-upload">
          <input
            type="file"
            accept="image/*"
            style="display: none"
            ref="fileInput"
            @change="handleAvatarChange"
          />
          <el-button size="small" type="primary" @click="fileInput?.click()">
            上传头像
          </el-button>
        </div>

        <!-- 编辑表单 -->
        <el-form
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

          <el-form-item label="角色">
            <el-input :model-value="roleText" disabled />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              保存修改
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 只读信息 -->
        <el-descriptions :column="1" border class="profile-descriptions">
          <el-descriptions-item label="注册时间">{{ userInfo.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="userInfo.status === 'ENABLED' ? 'success' : 'danger'" size="small">
              {{ userInfo.status === 'ENABLED' ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
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
/** 页面加载状态 */
const loading = ref(false)
/** 提交加载状态 */
const submitting = ref(false)
/** 表单引用 */
const formRef = ref<FormInstance>()
/** 文件输入引用 */
const fileInput = ref<HTMLInputElement>()

/** 当前用户信息（从 Store 获取） */
const userInfo = ref<User | null>(userStore.userInfo)

/** 表单数据 */
const form = reactive({
  username: '',
  email: '',
  phone: ''
})

/** 表单验证规则 */
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3-50 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

/** 角色中文文本 */
const roleText = computed(() => {
  return userInfo.value?.role === 'ADMIN' ? '管理员' : '普通用户'
})

/**
 * 初始化表单数据
 */
function initForm() {
  const user = userInfo.value
  if (user) {
    form.username = user.username || ''
    form.email = user.email || ''
    form.phone = user.phone || ''
  }
}

/**
 * 获取用户最新信息
 */
async function fetchProfile() {
  loading.value = true
  try {
    await userStore.getInfo()
    userInfo.value = userStore.userInfo
    initForm()
  } catch {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}

/**
 * 提交表单
 */
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
    // 更新本地用户信息
    if (res.data.data && userInfo.value) {
      Object.assign(userInfo.value, res.data.data)
      userStore.userInfo = { ...userInfo.value }
      localStorage.setItem('booknexus_user', JSON.stringify(userInfo.value))
    }
    ElMessage.success('资料更新成功')
  } catch {
    // Error already shown by interceptor
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单
 */
function resetForm() {
  initForm()
  formRef.value?.clearValidate()
}

/**
 * 处理头像文件选择
 */
async function handleAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
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
    .card-title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .profile-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
    padding-bottom: 20px;
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
    margin-bottom: 24px;
  }

  .profile-form {
    max-width: 480px;
    margin: 0 auto 24px;
    padding: 0 20px;
  }

  .profile-descriptions {
    margin-bottom: 24px;
  }
}
</style>
