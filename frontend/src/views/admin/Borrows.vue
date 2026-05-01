<template>
  <div class="borrows-page">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书名或用户名"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 160px">
          <el-option label="待确认" value="PENDING" />
          <el-option label="借出中" value="BORROWED" />
          <el-option label="已续借" value="RENEWED" />
          <el-option label="已归还" value="RETURNED" />
          <el-option label="逾期" value="OVERDUE" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="borrowList" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="借阅人" width="120">
          <template #default="{ row }">
            {{ row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="bookTitle" label="书名" min-width="180">
          <template #default="{ row }">
            {{ row.bookTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="borrowedAt" label="借阅日期" width="180" />
        <el-table-column prop="dueAt" label="到期日期" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="罚款(元)" width="100">
          <template #default="{ row }">
            {{ row.fineAmount != null ? `¥${row.fineAmount}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'BORROWED' || row.status === 'OVERDUE' || row.status === 'RENEWED'"
              type="primary"
              link
              @click="handleForceReturn(row)"
            >
              强制归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && borrowList.length === 0" description="暂无借阅记录" />

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchBorrows"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import type { BorrowRecord } from '@/types'

const loading = ref(false)
const borrowList = ref<BorrowRecord[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref('')

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

async function fetchBorrows() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value) params.status = statusFilter.value

    const res = await api.get('/v1/admin/borrows', { params })
    const data = res.data.data
    borrowList.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    borrowList.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchBorrows()
}

function resetSearch() {
  keyword.value = ''
  statusFilter.value = ''
  page.value = 1
  fetchBorrows()
}

async function handleForceReturn(row: BorrowRecord) {
  try {
    await ElMessageBox.confirm(`确定要强制归还「${row.bookTitle}」的借阅记录吗？`, '确认强制归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await api.put(`/v1/admin/borrows/${row.id}/return`)
    ElMessage.success('已强制归还')
    fetchBorrows()
  } catch {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchBorrows()
})
</script>

<style scoped lang="scss">
.borrows-page {
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
