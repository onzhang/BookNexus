<!--
  ============================================================
  MyBorrows.vue — 我的借阅记录
  @description 展示当前用户的借阅记录列表，含书籍封面/书名/作者/
               借阅日期/状态/罚款信息，支持续借和归还操作。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="borrows-page">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">我的借阅记录</span>
      </template>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="records.length === 0" class="empty-wrap">
        <el-empty description="暂无借阅记录" />
      </div>

      <template v-else>
        <!-- 借阅表格 -->
        <el-table
          :data="records"
          stripe
          border
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column label="封面" width="80">
            <template #default="{ row }">
              <div class="table-cover">
                <img v-if="row.bookCoverUrl" :src="row.bookCoverUrl" :alt="row.bookTitle" />
                <el-icon v-else :size="32"><Picture /></el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="bookTitle" label="书名" min-width="160" />
          <el-table-column prop="bookAuthor" label="作者" width="120" />
          <el-table-column prop="borrowedAt" label="借阅日期" width="180" />
          <el-table-column prop="dueAt" label="应还日期" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fineAmount" label="罚款(元)" width="100">
            <template #default="{ row }">
              {{ row.fineAmount != null ? `¥${row.fineAmount}` : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'BORROWED' || row.status === 'RENEWED'"
                type="primary"
                link
                @click="handleRenew(row.id)"
              >
                续借
              </el-button>
              <el-button
                v-if="row.status === 'BORROWED' || row.status === 'RENEWED' || row.status === 'PENDING'"
                type="success"
                link
                @click="handleReturn(row.id)"
              >
                归还
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @change="fetchRecords"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import type { BorrowRecordVO, PageResult } from '@/types'

/** 借阅记录列表 */
const records = ref<BorrowRecordVO[]>([])
/** 表格加载状态 */
const loading = ref(false)
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)

/** 借阅状态 → 中文文本映射 */
const statusTextMap: Record<string, string> = {
  PENDING: '待确认',
  BORROWED: '借阅中',
  RENEWED: '已续借',
  RETURNED: '已归还'
}

/** 借阅状态 → Element Plus Tag 类型映射 */
const statusTagMap: Record<string, string> = {
  PENDING: 'warning',
  BORROWED: '',
  RENEWED: 'success',
  RETURNED: 'info'
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

/** 获取借阅记录列表（分页） */
async function fetchRecords() {
  loading.value = true
  try {
    const res = await api.get<PageResult<BorrowRecordVO>>('/v1/user/borrows', {
      params: { page: page.value, size: size.value }
    })
    const data = res.data.data
    records.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 归还借阅
 * @param id - 借阅记录 ID
 */
async function handleReturn(id: number) {
  try {
    await api.put(`/v1/user/borrows/${id}/return`)
    ElMessage.success('归还成功')
    fetchRecords()
  } catch {
    // Error already shown by interceptor
  }
}

/**
 * 续借
 * @param id - 借阅记录 ID
 */
async function handleRenew(id: number) {
  try {
    await api.put(`/v1/user/borrows/${id}/renew`)
    ElMessage.success('续借成功')
    fetchRecords()
  } catch {
    // Error already shown by interceptor
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped lang="scss">
.borrows-page {
  max-width: 1200px;
  margin: 0 auto;

  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .loading-wrap {
    padding: 40px;
  }

  .empty-wrap {
    padding: 60px 0;
  }

  .table-cover {
    width: 48px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 4px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
