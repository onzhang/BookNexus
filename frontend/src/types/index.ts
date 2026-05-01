export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  avatar?: string
  role: 'ADMIN' | 'USER'
  status: number
  createdAt: string
  updatedAt: string
}

export interface Book {
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
  updatedAt: string
}

export interface BorrowRecord {
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

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId?: string
  timestamp?: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
}

export { ApiResponse, PageResult }
