<!--
  ============================================================
  Notifications.vue — 我的通知
  @description 展示当前用户的系统通知列表，含通知类型/标题/内容/
               已读状态/时间，支持标记已读操作。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="notifications-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的通知</span>
          <el-radio-group v-model="readFilter" size="small" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button :label="0">未读</el-radio-button>
            <el-radio-button :label="1">已读</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="records.length === 0" class="empty-wrap">
        <el-empty description="暂无通知" />
      </div>

      <template v-else>
        <!-- 通知表格 -->
        <el-table
          :data="records"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="typeTagType(row.type)" size="small">
                {{ typeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isRead === 1 ? 'info' : 'warning'" size="small">
                {{ row.isRead === 1 ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="通知时间" width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.isRead === 0"
                type="primary"
                link
                size="small"
                @click="handleMarkRead(row.id)"
              >
                标记已读
              </el-button>
              <span v-else class="read-text">已读</span>
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
import { ElMessage } from 'element-plus'
import api from '@/api'
import { UserAPI } from '@/api/endpoints'
import type { Notification, PageResult } from '@/types'

/** 通知记录列表 */
const records = ref<Notification[]>([])
/** 表格加载状态 */
const loading = ref(false)
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)
/** 已读状态筛选 */
const readFilter = ref<number | ''>('')

/** 通知类型 → 中文文本映射 */
const typeTextMap: Record<string, string> = {
  SYSTEM: '系统',
  SUBSCRIPTION: '订阅',
  OVERDUE: '逾期'
}

/** 通知类型 → Element Plus Tag 类型映射 */
const typeTagMap: Record<string, string> = {
  SYSTEM: 'primary',
  SUBSCRIPTION: 'success',
  OVERDUE: 'danger'
}

/**
 * 根据类型返回中文文本
 * @param type - 通知类型
 */
function typeText(type?: string) {
  if (!type) return '-'
  return typeTextMap[type] || type
}

/**
 * 根据类型返回标签类型
 * @param type - 通知类型
 */
function typeTagType(type?: string) {
  if (!type) return 'info'
  return typeTagMap[type] || 'info'
}

/** 获取通知记录列表（分页） */
async function fetchRecords() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (readFilter.value !== '') params.isRead = readFilter.value

    const res = await api.get<PageResult<Notification>>(UserAPI.NOTIFICATION_PAGE.path, { params })
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

/** 筛选条件变化 */
function handleFilterChange() {
  page.value = 1
  fetchRecords()
}

/**
 * 标记通知为已读
 * @param id - 通知 ID
 */
async function handleMarkRead(id: number) {
  try {
    await api.put(UserAPI.NOTIFICATION_READ(id).path)
    ElMessage.success('已标记为已读')
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
.notifications-page {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

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

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .read-text {
    color: #909399;
    font-size: 13px;
  }
}
</style>
