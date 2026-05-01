<!--
  ============================================================
  Home.vue — 用户端首页
  @description 图书浏览主页，支持搜索书名/作者，以卡片网格形式
               展示书籍列表，含封面占位、状态标签和分页功能。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <el-card shadow="hover" class="search-card">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书名或作者"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 书籍网格 -->
    <el-card shadow="hover" class="book-grid-card">
      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="books.length === 0" class="empty-wrap">
        <el-empty description="暂无书籍" />
      </div>

      <!-- 书籍卡片网格 -->
      <el-row v-else :gutter="20">
        <el-col
          v-for="book in books"
          :key="book.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="book-col"
        >
          <el-card
            shadow="hover"
            class="book-card"
            @click="goToDetail(book.id)"
          >
            <div class="book-cover">
              <img
                v-if="book.coverUrl"
                :src="book.coverUrl"
                :alt="book.title"
                class="cover-img"
              />
              <el-icon v-else :size="64" class="cover-placeholder"><Picture /></el-icon>
            </div>
            <div class="book-info">
              <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <el-tag
                :type="statusTagType(book.status)"
                size="small"
                class="book-status"
              >
                {{ statusText(book.status) }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[8, 12, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchBooks"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import api from '@/api'
import type { BookVO, PageResult } from '@/types'

const router = useRouter()
/** 书籍列表数据 */
const books = ref<BookVO[]>([])
/** 加载状态 */
const loading = ref(false)
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(8)
/** 总记录数 */
const total = ref(0)
/** 搜索关键字 */
const keyword = ref('')

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

/**
 * 跳转到书籍详情页
 * @param id - 书籍 ID
 */
function goToDetail(id: number) {
  router.push(`/user/books/${id}`)
}

/** 获取书籍列表（分页 + 搜索） */
async function fetchBooks() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get<PageResult<BookVO>>('/v1/public/books', { params })
    const data = res.data.data
    books.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    books.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  page.value = 1
  fetchBooks()
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped lang="scss">
.home-page {
  .search-card {
    margin-bottom: 20px;
  }

  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;

    .search-input {
      max-width: 400px;
    }
  }

  .loading-wrap {
    padding: 40px;
  }

  .empty-wrap {
    padding: 60px 0;
  }

  .book-col {
    margin-bottom: 20px;
  }

  .book-card {
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    height: 100%;

    &:hover {
      transform: translateY(-4px);
    }

    .book-cover {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 200px;
      background-color: #f5f7fa;
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 12px;

      .cover-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .cover-placeholder {
        color: var(--text-placeholder);
      }
    }

    .book-info {
      .book-title {
        font-size: 15px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .book-author {
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 8px;
      }

      .book-status {
        font-size: 12px;
      }
    }
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
