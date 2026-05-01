<!--
  ============================================================
  BookDetail.vue — 书籍详情页
  @description 展示书籍的详细信息（作者、ISBN、出版社、分类、简介等），
               提供借阅操作入口（可借时显示借阅按钮，已借出提示状态）。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="book-detail-page">
    <!-- 返回按钮 -->
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card v-loading="loading" shadow="hover" class="detail-card">
      <!-- 书籍详情 -->
      <template v-if="!loading && book">
        <div class="book-detail">
          <!-- 封面图 -->
          <div class="book-cover-large">
            <img
              v-if="book.coverUrl"
              :src="book.coverUrl"
              :alt="book.title"
              class="cover-img"
            />
            <el-icon v-else :size="128" class="cover-placeholder"><Picture /></el-icon>
          </div>
          <!-- 书籍元信息 -->
          <div class="book-meta">
            <h1 class="book-title">{{ book.title }}</h1>
            <div class="book-status-badge">
              <el-tag
                :type="statusTagType(book.status)"
                size="large"
              >
                {{ statusText(book.status) }}
              </el-tag>
            </div>
            <el-descriptions :column="1" border class="book-descriptions">
              <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
              <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
              <el-descriptions-item v-if="book.publisher" label="出版社">{{ book.publisher }}</el-descriptions-item>
              <el-descriptions-item v-if="book.categories?.length" label="分类">
                <el-tag
                  v-for="cat in book.categories"
                  :key="cat"
                  size="small"
                  class="category-tag"
                >
                  {{ cat }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="上架时间">{{ book.createdAt }}</el-descriptions-item>
            </el-descriptions>
            <!-- 内容简介 -->
            <div v-if="book.description" class="book-description">
              <h3>内容简介</h3>
              <p>{{ book.description }}</p>
            </div>
            <!-- 操作区 -->
            <div class="book-actions">
              <el-button
                v-if="book.status === 'AVAILABLE'"
                type="primary"
                size="large"
                :loading="borrowing"
                @click="handleBorrow"
              >
                借阅此书
              </el-button>
              <el-tag v-else-if="book.status === 'BORROWED'" type="warning" size="large">
                已借出
              </el-tag>
            </div>
          </div>
        </div>
      </template>

      <!-- 书籍不存在 -->
      <el-empty v-if="!loading && !book" description="书籍不存在" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import type { BookVO } from '@/types'

const route = useRoute()
const router = useRouter()

/** 当前书籍数据 */
const book = ref<BookVO | null>(null)
/** 页面加载状态 */
const loading = ref(false)
/** 借阅按钮加载状态 */
const borrowing = ref(false)

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

/** 返回上一页 */
function goBack() {
  router.back()
}

/** 获取书籍详情 */
async function fetchBook() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await api.get<BookVO>(`/v1/public/books/${id}`)
    book.value = res.data.data
  } catch {
    book.value = null
  } finally {
    loading.value = false
  }
}

/** 借阅当前书籍 */
async function handleBorrow() {
  if (!book.value) return
  borrowing.value = true
  try {
    await api.post('/v1/user/borrows', { bookId: book.value.id })
    ElMessage.success('借阅成功')
    fetchBook()
  } catch {
    // Error already shown by interceptor
  } finally {
    borrowing.value = false
  }
}

onMounted(() => {
  fetchBook()
})
</script>

<style scoped lang="scss">
.book-detail-page {
  max-width: 960px;
  margin: 0 auto;

  .back-row {
    margin-bottom: 16px;

    .el-button {
      font-size: 14px;
    }
  }

  .detail-card {
    min-height: 400px;
  }

  .book-detail {
    display: flex;
    gap: 32px;

    @media (max-width: 768px) {
      flex-direction: column;
    }
  }

  .book-cover-large {
    width: 300px;
    height: 400px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    border-radius: 8px;
    overflow: hidden;

    @media (max-width: 768px) {
      width: 100%;
      height: 300px;
    }

    .cover-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-placeholder {
      color: var(--text-placeholder);
    }
  }

  .book-meta {
    flex: 1;

    .book-title {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    .book-status-badge {
      margin-bottom: 20px;
    }

    .book-descriptions {
      margin-bottom: 20px;
    }

    .category-tag {
      margin-right: 6px;
    }

    .book-description {
      margin-bottom: 24px;
      padding: 16px;
      background: #f9fafb;
      border-radius: 8px;

      h3 {
        font-size: 15px;
        font-weight: 600;
        margin-bottom: 8px;
        color: var(--text-primary);
      }

      p {
        font-size: 14px;
        line-height: 1.8;
        color: var(--text-regular);
      }
    }

    .book-actions {
      padding-top: 12px;
    }
  }
}
</style>
