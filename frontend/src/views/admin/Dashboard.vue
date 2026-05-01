<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: rgba(64, 158, 255, 0.1); color: #409EFF;">
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
            <div class="stat-icon" style="background-color: rgba(103, 194, 58, 0.1); color: #67C23A;">
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
            <div class="stat-icon" style="background-color: rgba(230, 162, 60, 0.1); color: #E6A23C;">
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
            <div class="stat-icon" style="background-color: rgba(245, 108, 108, 0.1); color: #F56C6C;">
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

    <el-card shadow="hover" class="table-card">
      <template #header>
        <span class="card-title">最近借阅记录</span>
      </template>
      <el-table :data="borrowList" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="借阅人" width="120" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" />
        <el-table-column prop="borrowedAt" label="借阅日期" width="180" />
        <el-table-column prop="dueAt" label="到期日期" width="180" />
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Reading, User, Document, WarningFilled } from '@element-plus/icons-vue'
import api from '@/api'
import type { BorrowRecord } from '@/types'

const loading = ref(false)
const borrowList = ref<BorrowRecord[]>([])

const stats = reactive({
  totalBooks: 0,
  totalUsers: 0,
  borrowedCount: 0,
  overdueCount: 0
})

const statusTextMap: Record<string, string> = {
  PENDING: '待确认',
  BORROWED: '借出中',
  RENEWED: '已续借',
  RETURNED: '已归还',
  OVERDUE: '逾期'
}

const statusTagMap: Record<string, string> = {
  PENDING: 'warning',
  BORROWED: '',
  RENEWED: 'success',
  RETURNED: 'info',
  OVERDUE: 'danger'
}

function statusText(status: string) {
  return statusTextMap[status] || status
}

function statusTagType(status: string) {
  return statusTagMap[status] || 'info'
}

async function fetchStats() {
  try {
    const [bookRes, userRes, borrowRes, overdueRes] = await Promise.allSettled([
      api.get('/v1/admin/books', { params: { page: 1, size: 1 } }),
      api.get('/v1/admin/users', { params: { page: 1, size: 1 } }),
      api.get('/v1/admin/borrows', { params: { page: 1, size: 1, status: 'BORROWED' } }),
      api.get('/v1/admin/borrows', { params: { page: 1, size: 1, status: 'OVERDUE' } })
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

async function fetchRecentBorrows() {
  loading.value = true
  try {
    const res = await api.get('/v1/admin/borrows', { params: { page: 1, size: 5 } })
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
