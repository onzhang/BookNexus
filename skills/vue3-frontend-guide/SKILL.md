---
name: vue3-frontend-guide
description: Vue 3 + Vite + TypeScript 前端开发规范与最佳实践，涵盖项目结构、组件设计、状态管理、API 调用、样式规范与性能优化。
license: MIT
---

# Vue 3 前端开发指南

> 适用于 BookNexus 图书管理系统及其他 Vue 3 + TypeScript 技术栈项目的前端开发指导。

---

## 一、项目结构规范

### 1.1 标准目录结构

```
src/
├── api/                    # API 接口定义
│   ├── modules/            # 按模块划分 API
│   │   ├── book.ts
│   │   ├── user.ts
│   │   └── borrow.ts
│   └── index.ts           # API 导出
├── assets/                 # 静态资源
│   ├── images/
│   ├── styles/
│   │   └── variables.scss  # 全局样式变量
├── components/             # 公共组件
│   ├── common/             # 通用组件（Button、Input、Modal）
│   ├── layout/             # 布局组件（Header、Sidebar、Footer）
│   └── business/           # 业务组件（BookCard、UserAvatar）
├── composables/            # 组合式函数
│   ├── useAuth.ts
│   ├── usePagination.ts
│   └── useMessage.ts
├── config/                 # 配置文件
│   └── index.ts           # 环境变量配置
├── directives/             # 自定义指令
│   ├── permission.ts
│   └── loading.ts
├── hooks/                  # 钩子函数
├── layouts/                # 页面布局
├── router/                 # 路由配置
│   ├── index.ts
│   └── routes/
├── stores/                 # Pinia 状态管理
│   ├── user.ts
│   ├── book.ts
│   └── settings.ts
├── types/                  # TypeScript 类型定义
│   ├── api.d.ts
│   ├── components.d.ts
│   └── global.d.ts
├── utils/                  # 工具函数
│   ├── storage.ts
│   ├── format.ts
│   └── validate.ts
├── views/                  # 页面组件
│   ├── auth/
│   ├── book/
│   └── dashboard/
├── App.vue
└── main.ts
```

### 1.2 文件命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase + 组件含义 | `BookCard.vue`、`UserAvatar.vue` |
| 组合式函数 | use + 名称 | `useAuth.ts`、`usePagination.ts` |
| 工具函数 | 驼峰式 | `storage.ts`、`formatDate.ts` |
| API 模块 | 驼峰式 | `bookApi.ts`、`userApi.ts` |
| 样式文件 | kebab-case | `common-styles.scss`、`variables.scss` |

---

## 二、组件开发规范

### 2.1 组件结构

```vue
<template>
  <div class="book-card">
    <img :src="book.coverUrl" :alt="book.title" />
    <h3>{{ book.title }}</h3>
    <p>{{ book.author }}</p>
    <slot name="footer"></slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Book } from '@/types'

interface Props {
  book: Book
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  clickable: false
})

const displayTitle = computed(() => {
  return props.book.title.length > 20
    ? props.book.title.slice(0, 20) + '...'
    : props.book.title
})

defineEmits<{
  click: [book: Book]
}>()

defineExpose({
  book: props.book
})
</script>

<style scoped lang="scss">
.book-card {
  padding: 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  img {
    width: 100%;
    height: 200px;
    object-fit: cover;
  }

  h3 {
    margin: 12px 0 8px;
    font-size: 16px;
  }

  p {
    color: #666;
    font-size: 14px;
  }
}
</style>
```

### 2.2 组件Props定义

```typescript
// ✅ 使用 TypeScript 接口定义 Props
interface Props {
  title: string
  count?: number
  list: string[]
  status: 'pending' | 'success' | 'failed'
  onChange?: (value: string) => void
}

// ✅ 使用 withDefaults 定义默认值
withDefaults(defineProps<Props>(), {
  count: 0,
  onChange: () => {}
})

// ❌ 避免使用复杂类型推断
// const props = defineProps<{ ... }>()
```

### 2.3 组件事件

```typescript
// ✅ 使用 defineEmits 定义事件
const emit = defineEmits<{
  update: [value: string]
  delete: []
}>()

// 在方法中调用
const handleClick = () => {
  emit('update', 'new value')
}

// ❌ 避免使用 $emit
// this.$emit('update', 'new value')
```

---

## 三、TypeScript 类型定义

### 3.1 API 响应类型

```typescript
// 统一响应格式
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 分页响应
interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

// 请求错误
interface ApiError {
  code: number
  message: string
}
```

### 3.2 业务类型定义

```typescript
// 用户类型
interface User {
  id: number
  username: string
  email: string
  phone?: string
  avatar?: string
  role: 'ADMIN' | 'USER'
  status: number
  createdAt: string
}

// 图书类型
interface Book {
  id: number
  title: string
  author: string
  isbn: string
  publisher?: string
  categoryId?: number
  categoryName?: string
  stock: number
  availableStock: number
  price: number
  description?: string
  coverUrl?: string
  createdAt: string
}

// 借阅记录
interface BorrowRecord {
  id: number
  userId: number
  userName: string
  bookId: number
  bookTitle: string
  borrowedAt: string
  dueAt: string
  returnedAt?: string
  status: 'BORROWED' | 'RETURNED' | 'OVERDUE'
}
```

### 3.3 请求类型

```typescript
// 创建请求
interface CreateBookRequest {
  title: string
  author: string
  isbn: string
  price: number
  stock: number
  categoryId?: number
  description?: string
  coverUrl?: string
}

// 更新请求
interface UpdateBookRequest {
  title?: string
  author?: string
  price?: number
  stock?: number
}

// 分页请求
interface PageRequest {
  page: number
  size: number
  keyword?: string
}
```

---

## 四、API 调用规范

### 4.1 API 模块划分

```typescript
// api/modules/book.ts
import request from '@/utils/request'
import type { Book, CreateBookRequest, UpdateBookRequest, PageRequest, PageResult } from '@/types'

export const bookApi = {
  // 获取图书列表
  getBooks(params: PageRequest) {
    return request.get<ApiResponse<PageResult<Book>>>('/api/books', { params })
  },

  // 获取图书详情
  getBookById(id: number) {
    return request.get<ApiResponse<Book>>(`/api/books/${id}`)
  },

  // 创建图书
  createBook(data: CreateBookRequest) {
    return request.post<ApiResponse<number>>('/api/books', data)
  },

  // 更新图书
  updateBook(id: number, data: UpdateBookRequest) {
    return request.put<ApiResponse<void>>(`/api/books/${id}`, data)
  },

  // 删除图书
  deleteBook(id: number) {
    return request.delete<ApiResponse<void>>(`/api/books/${id}`)
  }
}
```

### 4.2 Request 封装

```typescript
// utils/request.ts
import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
```

### 4.3 使用示例

```typescript
// 在组件中使用
import { bookApi } from '@/api/modules/book'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const bookList = ref<Book[]>([])

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await bookApi.getBooks({ page: 1, size: 10 })
    bookList.value = res.data.records
  } catch (error) {
    console.error('获取图书列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  try {
    await bookApi.createBook({ title: '新书', author: '作者', price: 99.9 })
    ElMessage.success('创建成功')
    fetchBooks()
  } catch (error) {
    // 错误已在拦截器处理
  }
}
```

---

## 五、状态管理（Pinia）

### 5.1 Store 定义

```typescript
// stores/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, LoginRequest } from '@/types'
import { userApi } from '@/api/modules/user'
import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<User | null>(null)

  const login = async (data: LoginRequest) => {
    const res = await userApi.login(data)
    token.value = res.data.token
    setToken(res.data.token)
    await fetchUserInfo()
  }

  const fetchUserInfo = async () => {
    const res = await userApi.getUserInfo()
    userInfo.value = res.data
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  return {
    token,
    userInfo,
    login,
    logout,
    fetchUserInfo,
    isLoggedIn,
    isAdmin
  }
})
```

### 5.2 在组件中使用

```typescript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 访问状态
console.log(userStore.userInfo)
console.log(userStore.isAdmin)

// 调用方法
await userStore.login({ username: 'admin', password: '123456' })
userStore.logout()
```

---

## 六、路由与权限

### 6.1 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'books',
        name: 'BookList',
        component: () => import('@/views/book/index.vue'),
        meta: { title: '图书管理', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo?.role)) {
    next('/403')
  } else {
    next()
  }
})

export default router
```

---

## 七、样式规范

### 7.1 CSS 变量

```scss
// assets/styles/variables.scss
:root {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;

  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  --border-color: #dcdfe6;
  --bg-color: #f5f7fa;

  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
}
```

### 7.2 组件样式

```vue
<style scoped lang="scss">
// 使用 scoped 避免样式污染
// 使用 lang="scss" 使用 SCSS 语法

.card {
  padding: var(--spacing-md);
  background: #fff;
  border-radius: 8px;

  &__header {
    display: flex;
    justify-content: space-between;
    margin-bottom: var(--spacing-md);
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
  }

  &__content {
    color: var(--text-regular);
  }
}
</style>
```

### 7.3 响应式布局

```scss
.container {
  display: flex;
  gap: var(--spacing-md);

  @media (max-width: 768px) {
    flex-direction: column;
  }

  @media (min-width: 1200px) {
    max-width: 1200px;
    margin: 0 auto;
  }
}
```

---

## 八、性能优化

### 8.1 图片懒加载

```vue
<img v-lazy="book.coverUrl" />

// directives/lazy.ts
export default {
  mounted(el: HTMLImageElement, binding: DirectiveBinding) {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          el.src = binding.value
          observer.disconnect()
        }
      },
      { rootMargin: '50px' }
    )
    observer.observe(el)
  }
}
```

### 8.2 组件懒加载

```typescript
// ✅ 路由级懒加载
const BookDetail = () => import('@/views/book/Detail.vue')

// ✅ 条件渲染使用 v-if
<template v-if="showModal">
  <ComplexModal />
</template>

// ✅ 使用 keep-alive 缓存
<keep-alive include="BookList">
  <router-view />
</keep-alive>
```

### 8.3 防抖与节流

```typescript
// composables/useDebounce.ts
export function useDebounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
) {
  let timer: ReturnType<typeof setTimeout> | null = null

  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

// 使用
const debouncedSearch = useDebounce(handleSearch, 500)
```

---

## 九、代码规范

### 9.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | PascalCase | `BookCard.vue` |
| 组合式函数 | use + PascalCase | `useAuth.ts` |
| 常量 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| 接口 | PascalCase + Interface | `UserInfoInterface` |
| 类型 | PascalCase | `BookStatus` |
| 枚举 | PascalCase | `BookStatusEnum` |
| 样式类 | kebab-case | `.book-card` |

### 9.2 导入顺序

```typescript
// 1. Vue / Vue生态
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

// 2. 第三方库
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

// 3. 内部模块
import { bookApi } from '@/api/modules/book'
import { useUserStore } from '@/stores/user'
import type { Book } from '@/types'

// 4. 工具函数
import { formatDate } from '@/utils/format'
```

### 9.3 注释规范

```typescript
// ✅ 函数注释
/**
 * 获取图书列表
 * @param params 分页参数
 * @returns 图书分页数据
 */
async getBooks(params: PageRequest): Promise<PageResult<Book>>

// ❌ 避免无意义的注释
// 获取图书
async getBooks() { ... }
```

---

## 十、常用工具函数

### 10.1 格式化工具

```typescript
// utils/format.ts
import dayjs from 'dayjs'

export function formatDate(date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  return dayjs(date).format(format)
}

export function formatMoney(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

export function truncateText(text: string, maxLength: number): string {
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}
```

### 10.2 校验工具

```typescript
// utils/validate.ts
export function isEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export function isPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

export function isUrl(url: string): boolean {
  return /^https?:\/\/.+/.test(url)
}
```

---

## 十一、代码审查 Checklist

- [ ] 组件使用 `<script setup>` + TypeScript
- [ ] Props 定义使用 interface + withDefaults
- [ ] API 调用统一封装在 api/modules 下
- [ ] 敏感信息不硬编码，使用环境变量
- [ ] 图片使用懒加载
- [ ] 避免在模板中使用复杂表达式
- [ ] 样式使用 scoped，避免全局污染
- [ ] 使用 Pinia 管理全局状态
- [ ] 路由配置懒加载组件
- [ ] 错误处理完善，有 loading 状态

---

## 参考资源

- [Vue 3 Documentation](https://vuejs.org/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Vite Documentation](https://vitejs.dev/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Element Plus](https://element-plus.org/)