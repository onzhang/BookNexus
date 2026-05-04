/**
 * BookNexus — 用户认证状态（Pinia Store）
 *
 * @description 管理用户登录/注册/登出流程，持久化 Token 和用户信息到 localStorage，
 *              提供 isLoggedIn、isAdmin 等计算属性供全局访问。
 * @author 张俊文
 * @date 2026-05-01
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest, LoginResponse } from '@/types'
import request, { TOKEN_KEY } from '@/api'
import { PublicAPI, UserAPI } from '@/api/endpoints'

/** localStorage 中存储用户信息的键名 */
const USER_KEY = 'booknexus_user'
/** localStorage 中存储 Refresh Token 的键名 */
const REFRESH_TOKEN_KEY = 'booknexus_refresh_token'

/** 用户认证 Store */
export const useUserStore = defineStore('user', () => {
  /** JWT Token，从 localStorage 初始化 */
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  /** 当前用户信息，从 localStorage 初始化 */
  const userInfo = ref<User | null>(
    JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  )

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 是否为管理员 */
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  /**
   * 用户登录
   * @param data - 登录请求（用户名 + 密码）
   */
  async function login(data: LoginRequest) {
    const res = await request.post<LoginResponse>(PublicAPI.AUTH_LOGIN.path, data)
    const { accessToken, refreshToken, userId, username, role } = res.data.data
    token.value = accessToken
    userInfo.value = { id: userId, username, role, status: 1, createdAt: '', updatedAt: '' } as User
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  /**
   * 用户注册
   * @param data - 注册请求（用户名 + 密码 + 邮箱）
   */
  async function register(data: RegisterRequest) {
    const res = await request.post<LoginResponse>(PublicAPI.AUTH_REGISTER.path, data)
    const { accessToken, refreshToken, userId, username, role } = res.data.data
    token.value = accessToken
    userInfo.value = { id: userId, username, role, status: 1, createdAt: '', updatedAt: '' } as User
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  /** 获取当前用户详细信息（调用 /me 接口） */
  async function getInfo() {
    const res = await request.get<ApiResponse<User>>(UserAPI.AUTH_ME.path)
    userInfo.value = res.data.data
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  /** 登出：清除 Token 和用户信息 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
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
