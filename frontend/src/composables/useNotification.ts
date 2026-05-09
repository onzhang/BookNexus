/**
 * BookNexus — SSE 通知连接管理（Composable）
 *
 * @description 管理 Server-Sent Events（SSE）连接生命周期的 Vue composable，
 *              采用模块级单例模式，确保同一时间仅维持一个 EventSource 连接。
 *              提供自动重连机制（最多 5 次），重连间隔 3 秒。
 * @author 张俊文
 * @date 2026-05-08
 */

import { ref, onUnmounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import type { Notification } from '@/types'

/** SSE 后端订阅地址 */
const SSE_URL = '/api/v1/user/notifications/subscribe'

/** 重连间隔（毫秒） */
const RECONNECT_DELAY = 3000

/** 最大重连次数 */
const MAX_RETRIES = 5

// ==================== 模块级单例状态 ====================

/** EventSource 实例（单例，模块内共享） */
let eventSource: EventSource | null = null

/** 当前重连次数 */
let retryCount = 0

/** 连接状态（ref，供所有消费者共享） */
const isConnected = ref(false)

// ==================== 内部函数 ====================

/**
 * 处理收到的 SSE 通知事件
 * @param event - 原始 MessageEvent，其 data 属性为 JSON 字符串
 */
function handleNotificationEvent(event: MessageEvent) {
  try {
    const notification: Notification = JSON.parse(event.data)
    const notificationStore = useNotificationStore()

    // 重置重连计数 — 成功接收到消息说明连接正常
    retryCount = 0

    // 将通知加入弹出队列，并更新未读计数
    notificationStore.addToQueue(notification)
    notificationStore.setUnreadCount(notificationStore.unreadCount + 1)

    console.log('[SSE] 收到通知:', notification.title)
  } catch (err) {
    console.error('[SSE] 解析通知数据失败:', err)
  }
}

/**
 * 处理 SSE 连接错误事件，触发自动重连
 */
function handleErrorEvent() {
  console.warn(`[SSE] 连接错误（第 ${retryCount + 1} 次）`)

  // 关闭当前连接
  cleanup()

  if (retryCount < MAX_RETRIES) {
    retryCount++
    isConnected.value = false
    useNotificationStore().setConnected(false)

    console.log(`[SSE] 将在 ${RECONNECT_DELAY / 1000} 秒后重连（第 ${retryCount} 次）`)
    setTimeout(() => {
      connectInternal()
    }, RECONNECT_DELAY)
  } else {
    console.error(`[SSE] 已达最大重连次数（${MAX_RETRIES}），停止重连`)
    isConnected.value = false
    useNotificationStore().setConnected(false)
  }
}

/**
 * 内部连接函数 — 创建 EventSource 并绑定事件监听
 */
function connectInternal() {
  // 防止重复创建
  if (eventSource) {
    return
  }

  try {
    eventSource = new EventSource(SSE_URL)

    eventSource.addEventListener('notification', handleNotificationEvent)

    eventSource.onopen = () => {
      console.log('[SSE] 连接已建立')
      isConnected.value = true
      useNotificationStore().setConnected(true)
    }

    eventSource.onerror = handleErrorEvent
  } catch (err) {
    console.error('[SSE] 创建 EventSource 失败:', err)
  }
}

/**
 * 清理当前 EventSource 连接（不触发重连）
 */
function cleanup() {
  if (eventSource) {
    console.log('[SSE] 关闭连接')
    eventSource.removeEventListener('notification', handleNotificationEvent)
    eventSource.onopen = null
    eventSource.onerror = null
    eventSource.close()
    eventSource = null
  }
}

// ==================== 导出函数（Composable 公共 API） ====================

/**
 * 建立 SSE 连接
 * 如果已有连接则不做任何操作（单例模式）
 */
function connect() {
  if (eventSource) {
    console.log('[SSE] 已有连接，跳过重复创建')
    return
  }
  retryCount = 0
  connectInternal()
}

/**
 * 断开 SSE 连接，重置重连计数
 */
function disconnect() {
  cleanup()
  retryCount = 0
  isConnected.value = false
  useNotificationStore().setConnected(false)
  console.log('[SSE] 已断开连接')
}

/**
 * SSE 通知 Composable
 *
 * 返回 { connect, disconnect, isConnected } 供 Vue 组件使用。
 * isConnected 为模块级共享 ref，所有调用方读取同一状态。
 *
 * @example
 * ```typescript
 * const { connect, disconnect, isConnected } = useNotification()
 * onMounted(() => connect())
 * ```
 */
export function useNotification() {
  // 在组件卸载时自动断开连接
  onUnmounted(() => {
    disconnect()
  })

  return {
    connect,
    disconnect,
    isConnected
  }
}
