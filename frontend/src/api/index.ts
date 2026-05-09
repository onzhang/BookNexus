/**
 * BookNexus — Axios HTTP 客户端
 *
 * @description 创建并配置 Axios 实例，封装统一的请求/响应拦截器。
 *              - 请求拦截器：自动携带 JWT Token
 *              - 响应拦截器：统一错误处理、401 自动跳转登录
 * @author 张俊文
 * @date 2026-05-01
 */

import axios, { type AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'

/** localStorage 中存储 Token 的键名 */
const TOKEN_KEY = 'booknexus_token'

/** Axios 实例 — 带默认配置 */
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器 — 自动从 localStorage 读取 Token 并注入 Authorization 请求头
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器 — 统一处理业务错误（code !== 200）和 HTTP 错误（401 过期等）
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200 && res.code !== 201) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    const backendMsg = error.response?.data?.message
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('booknexus_user')
      window.location.href = '/login'
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 400) {
      ElMessage.error(backendMsg || '请求参数错误')
    } else if (status === 404) {
      ElMessage.error(backendMsg || '资源不存在')
    } else if (status === 409) {
      ElMessage.error(backendMsg || '操作冲突')
    } else if (status === 500) {
      ElMessage.error(backendMsg || '服务器内部错误')
    } else {
      ElMessage.error(backendMsg || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export { TOKEN_KEY }
export default request
