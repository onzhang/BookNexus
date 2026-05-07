<!--
  ============================================================
  Dashboard.vue — 管理端仪表盘
  @description 管理员首页，展示系统概览统计卡片（总藏书、总用户、
               借出中、逾期未还）和最近借阅记录列表。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="dashboard">
    <!-- 统计卡片行 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--accent-bg); color: var(--primary-color);">
              <el-icon :size="28"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总藏书</div>
              <div class="stat-value">{{ stats.totalBooks }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--success-bg); color: var(--success-color);">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总用户</div>
              <div class="stat-value">{{ stats.totalUsers }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--warning-bg); color: var(--warning-color);">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">借出中</div>
              <div class="stat-value">{{ stats.borrowedCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--danger-bg); color: var(--danger-color);">
              <el-icon :size="28"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">逾期未还</div>
              <div class="stat-value">{{ stats.overdueCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近借阅记录表格 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <span class="card-title">最近借阅记录</span>
      </template>
      <el-table :data="borrowList" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="借阅人" width="120" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" />
        <el-table-column prop="borrowDate" label="借阅日期" width="180" />
        <el-table-column prop="dueDate" label="到期日期" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fineAmount" label="罚款(元)" width="100">
          <template #default="{ row }">
            {{ row.fineAmount ? `¥${row.fineAmount}` : '-' }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && borrowList.length === 0" description="暂无借阅记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Reading, User, Document, WarningFilled } from '@element-plus/icons-vue'
import api from '@/api'
import { AdminAPI } from '@/api/endpoints'
import type { BorrowRecord } from '@/types'

/** 表格加载状态 */
const loading = ref(false)
/** 最近借阅列表 */
const borrowList = ref<BorrowRecord[]>([])

/** 统计面板数据 */
const stats = reactive({
  totalBooks: 0,
  totalUsers: 0,
  borrowedCount: 0,
  overdueCount: 0
})

/** 借阅状态 → 中文文本映射 */
const statusTextMap: Record<string, string> = {
  PENDING: '待确认',
  BORROWED: '借出中',
  RENEWED: '已续借',
  RETURNED: '已归还',
  OVERDUE: '逾期'
}

/** 借阅状态 → Element Plus Tag 类型映射 */
const statusTagMap: Record<string, string> = {
  PENDING: 'warning',
  BORROWED: '',
  RENEWED: 'success',
  RETURNED: 'info',
  OVERDUE: 'danger'
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

/** 获取四项统计数据（并行请求，单项失败不影响其他） */
async function fetchStats() {
  try {
    const [bookRes, userRes, borrowRes, overdueRes] = await Promise.allSettled([
      api.get(AdminAPI.BOOK_PAGE.path, { params: { page: 1, size: 1 } }),
      api.get(AdminAPI.USER_PAGE.path, { params: { page: 1, size: 1 } }),
      api.get(AdminAPI.BORROW_PAGE.path, { params: { page: 1, size: 1, status: 'BORROWED' } }),
      api.get(AdminAPI.BORROW_PAGE.path, { params: { page: 1, size: 1, status: 'OVERDUE' } })
    ])

    if (bookRes.status === 'fulfilled') {
      stats.totalBooks = bookRes.value.data.data?.total ?? 0
    }
    if (userRes.status === 'fulfilled') {
      stats.totalUsers = userRes.value.data.data?.total ?? 0
    }
    if (borrowRes.status === 'fulfilled') {
      stats.borrowedCount = borrowRes.value.data.data?.total ?? 0
    }
    if (overdueRes.status === 'fulfilled') {
      stats.overdueCount = overdueRes.value.data.data?.total ?? 0
    }
  } catch {
    // Stats will remain 0 on failure
  }
}

/** 获取最近 5 条借阅记录 */
async function fetchRecentBorrows() {
  loading.value = true
  try {
    const res = await api.get(AdminAPI.BORROW_PAGE.path, { params: { page: 1, size: 5 } })
    borrowList.value = res.data.data?.records ?? []
  } catch {
    borrowList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentBorrows()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .stat-info {
      .stat-label {
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--text-primary);
      }
    }
  }

  .table-card {
    .card-title {
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>
