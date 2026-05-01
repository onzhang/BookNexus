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

export interface BookForm {
  title: string
  author: string
  isbn: string
  publisher?: string
  categoryId?: number
  stock: number
  price: number
  description?: string
  coverUrl?: string
}

export interface BookVO {
  id: number
  title: string
  author: string
  isbn: string
  publisher?: string
  description?: string
  coverUrl?: string
  status: 'AVAILABLE' | 'BORROWED' | 'DAMAGED' | 'LOST'
  categories?: string[]
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

export interface BorrowRecordVO {
  id: number
  bookId: number
  bookTitle: string
  bookAuthor?: string
  bookCoverUrl?: string
  borrowedAt: string
  dueAt: string
  returnedAt?: string
  status: 'PENDING' | 'BORROWED' | 'RENEWED' | 'RETURNED'
  fineAmount?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface ApiResponse<T> {
  code: number | string
  message: string
  data: T
  requestId?: string
  timestamp?: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  role: 'ADMIN' | 'USER'
}
