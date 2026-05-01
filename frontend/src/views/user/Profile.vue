<template>
  <div class="profile-page">
    <el-card v-loading="loading" shadow="hover" class="profile-card">
      <template #header>
        <span class="card-title">个人中心</span>
      </template>

      <template v-if="!loading && userInfo">
        <div class="profile-header">
          <el-avatar :size="80" class="profile-avatar">
            {{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <div class="profile-name">
            <h2>{{ userInfo.username }}</h2>
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : ''" size="small">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="1" border class="profile-descriptions">
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userInfo.email || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ userInfo.phone || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ userInfo.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'" size="small">
              {{ userInfo.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="profile-actions">
          <el-button type="primary" disabled>编辑资料</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && !userInfo" description="无法获取用户信息" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)

const userInfo = ref(userStore.userInfo)

async function fetchProfile() {
  loading.value = true
  try {
    await userStore.getInfo()
    userInfo.value = userStore.userInfo
  } catch {
    // Error already shown by interceptor
  } finally {
    loading.value = false
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
      color: #fff;
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

  .profile-descriptions {
    margin-bottom: 24px;
  }

  .profile-actions {
    padding-top: 8px;
  }
}
</style>
