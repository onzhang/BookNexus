import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, LoginResponse } from '@/types'
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
    const res = await request.post<LoginResponse>('/auth/login', data)
    token.value = res.data.data.token
    userInfo.value = res.data.data.user
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  async function getInfo() {
    const res = await request.get<ApiResponse<User>>('/auth/userinfo')
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
    getInfo,
    logout
  }
})
