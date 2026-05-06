/**
 * BookNexus — 前端类型定义
 *
 * @description 集中定义项目中所有实体/请求/响应的 TypeScript 接口，
 *              确保前后端数据结构一致，提供完整的类型安全支持。
 * @author 张俊文
 * @date 2026-05-01
 */

/** 用户实体 */
export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  avatarUrl?: string
  /** 角色：管理员 / 普通用户 */
  role: 'ADMIN' | 'USER'
  /** 状态：ENABLED=正常，DISABLED=禁用 */
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
  updatedAt: string
}

/** 书籍实体（管理端使用，含库存信息） */
export interface Book {
  id: number
  title: string
  author: string
  isbn: string
  publisher?: string
  categoryId?: number
  categoryName?: string
  /** 总库存 */
  stock: number
  /** 当前可借库存 */
  availableStock: number
  description?: string
  coverUrl?: string
  createdAt: string
  updatedAt: string
}

/** 书籍表单（新增/编辑） */
export interface BookForm {
  title: string
  author: string
  isbn: string
  publisher?: string
  categoryId?: number
  stock: number
  description?: string
  coverUrl?: string
}

/** 书籍视图对象（用户端展示，含状态和分类） */
export interface BookVO {
  id: number
  title: string
  author: string
  isbn: string
  publisher?: string
  description?: string
  coverUrl?: string
  status: 'AVAILABLE' | 'BORROWED' | 'DAMAGED' | 'LOST'
  categoryNames?: string[]
  createdAt: string
  updatedAt: string
}

/** 借阅记录（管理端使用） */
export interface BorrowRecord {
  id: number
  userId: number
  username: string
  bookId: number
  bookTitle: string
  borrowDate: string
  dueDate: string
  returnDate?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'BORROWED' | 'RENEWED' | 'RETURNED' | 'OVERDUE'
}

/** 借阅记录视图对象（用户端使用，含罚款和封面信息） */
export interface BorrowRecordVO {
  id: number
  bookId: number
  bookTitle: string
  bookAuthor?: string
  bookCoverUrl?: string
  borrowDate: string
  dueDate: string
  returnDate?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'BORROWED' | 'RENEWED' | 'RETURNED'
  fineAmount?: number
}

/** 通用分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

/** 统一 API 响应格式 */
export interface ApiResponse<T> {
  code: number | string
  message: string
  data: T
  requestId?: string
  timestamp?: number
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  password: string
  email?: string
}

/** 收藏记录视图对象 */
export interface FavoriteVO {
  id: number
  userId: number
  bookId: number
  bookTitle: string
  bookAuthor?: string
  bookCoverUrl?: string
  bookStatus?: 'AVAILABLE' | 'BORROWED' | 'DAMAGED' | 'LOST'
  createdAt: string
}

/** 订阅记录视图对象 */
export interface SubscriptionVO {
  id: number
  userId: number
  bookId: number
  bookTitle: string
  bookAuthor?: string
  bookCoverUrl?: string
  isActive: number
  createdAt: string
  updatedAt?: string
}

/** 书架实体 */
export interface Bookshelf {
  id: number
  name: string
  location?: string
  description?: string
  createdAt: string
  updatedAt: string
}

/** 书架表单（新增/编辑） */
export interface BookshelfForm {
  name: string
  location?: string
  description?: string
}

/** 分类实体 */
export interface Category {
  id: number
  name: string
  parentId: number
  parentName?: string
  sortOrder: number
  children?: Category[]
  createdAt: string
  updatedAt: string
}

/** 分类表单（新增/编辑） */
export interface CategoryForm {
  name: string
  parentId: number
  sortOrder: number
}

/** 登录响应 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  role: 'ADMIN' | 'USER'
}

/** 公告实体 */
export interface Announcement {
  id: number
  title: string
  content: string
  publisherId: number
  isPublished: number
  createdAt: string
  updatedAt: string
}

/** 公告表单（新增/编辑） */
export interface AnnouncementForm {
  title: string
  content: string
  isPublished: number
}

/** 通知实体 */
export interface Notification {
  id: number
  userId: number
  type: 'SYSTEM' | 'SUBSCRIPTION' | 'OVERDUE'
  title: string
  content?: string
  isRead: number
  createdAt: string
  updatedAt: string
}

/** 留言实体 */
export interface Message {
  id: number
  userId: number
  content: string
  reply?: string
  replyAt?: string
  replierId?: number
  createdAt: string
  updatedAt: string
}

/** 留言回复表单 */
export interface MessageReplyForm {
  reply: string
}
