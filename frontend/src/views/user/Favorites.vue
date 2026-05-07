<!--
  ============================================================
  Favorites.vue — 我的收藏
  @description 展示当前用户的图书收藏列表，含书籍封面/书名/作者/
                收藏时间/状态，支持取消收藏操作。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="favorites-page">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">我的收藏</span>
      </template>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="records.length === 0" class="empty-wrap">
        <el-empty description="暂无收藏图书" />
      </div>

      <template v-else>
        <!-- 收藏表格 -->
        <el-table
          :data="records"
          stripe
          border
          style="width: 100%"
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
          <el-table-column prop="createdAt" label="收藏时间" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.bookStatus)" size="small">
                {{ statusText(row.bookStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="goToBook(row.bookId)">
                查看详情
              </el-button>
              <el-button type="danger" link @click="handleRemove(row.bookId)">
                取消收藏
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
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import { UserAPI } from '@/api/endpoints'
import type { FavoriteVO, PageResult } from '@/types'

const router = useRouter()

/** 收藏记录列表 */
const records = ref<FavoriteVO[]>([])
/** 表格加载状态 */
const loading = ref(false)
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)

/** 书籍状态 → 中文文本映射 */
const statusTextMap: Record<string, string> = {
  AVAILABLE: '可借',
  BORROWED: '已借出',
  DAMAGED: '损坏',
  LOST: '丢失'
}

/** 书籍状态 → Element Plus Tag 类型映射 */
const statusTagMap: Record<string, string> = {
  AVAILABLE: 'success',
  BORROWED: 'warning',
  DAMAGED: 'danger',
  LOST: 'info'
}

/**
 * 根据状态码返回中文文本
 * @param status - 状态码
 */
function statusText(status?: string) {
  if (!status) return '-'
  return statusTextMap[status] || status
}

/**
 * 根据状态码返回标签类型
 * @param status - 状态码
 */
function statusTagType(status?: string) {
  if (!status) return 'info'
  return statusTagMap[status] || 'info'
}

/** 获取收藏记录列表（分页） */
async function fetchRecords() {
  loading.value = true
  try {
    const res = await api.get<PageResult<FavoriteVO>>(UserAPI.FAVORITE_MY_PAGE.path, {
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
 * 跳转书籍详情页
 * @param bookId - 书籍 ID
 */
function goToBook(bookId: number) {
  router.push(`/user/books/${bookId}`)
}

/**
 * 取消收藏
 * @param bookId - 图书 ID
 */
async function handleRemove(bookId: number) {
  try {
    await ElMessageBox.confirm('确定要取消收藏该图书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await api.delete(UserAPI.FAVORITE_DELETE(bookId).path)
    ElMessage.success('已取消收藏')
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
.favorites-page {
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
    background: var(--bg-card);
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
