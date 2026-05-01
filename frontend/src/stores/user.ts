import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest, LoginResponse } from '@/types'
import request, { TOKEN_KEY } from '@/api'

const USER_KEY = 'booknexus_user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<User | null>(
    JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  )

  const isLoggedIn = computed(() => !!token.value)

  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  async function login(data: LoginRequest) {
    const res = await request.post<LoginResponse>('/v1/public/auth/login', data)
    const { accessToken, userId, username, role } = res.data.data
    token.value = accessToken
    userInfo.value = { id: userId, username, role, status: 1, createdAt: '', updatedAt: '' } as User
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  async function register(data: RegisterRequest) {
    const res = await request.post<LoginResponse>('/v1/public/auth/register', data)
    const { accessToken, userId, username, role } = res.data.data
    token.value = accessToken
    userInfo.value = { id: userId, username, role, status: 1, createdAt: '', updatedAt: '' } as User
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  async function getInfo() {
    const res = await request.get<ApiResponse<User>>('/v1/user/auth/me')
    userInfo.value = res.data.data
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    getInfo,
    logout
  }
})
