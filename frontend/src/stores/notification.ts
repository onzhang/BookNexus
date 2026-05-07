/**
 * BookNexus — 通知状态（Pinia Store）
 *
 * @description 管理通知相关的全局 UI 状态，包括未读数量、SSE 连接状态、
 *              弹出通知队列等。不负责数据获取，仅处理通知展现层状态。
 * @author 张俊文
 * @date 2026-05-08
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification } from '@/types'

/** 弹出队列最大长度 */
const MAX_POPUP_QUEUE = 10

/** 通知状态 Store */
export const useNotificationStore = defineStore('notification', () => {
  /** 未读通知数量 */
  const unreadCount = ref(0)
  /** SSE 连接状态 */
  const isConnected = ref(false)
  /** 弹出通知队列（最多保留 MAX_POPUP_QUEUE 条） */
  const popupQueue = ref<Notification[]>([])
  /** 最近通知列表 */
  const latestNotifications = ref<Notification[]>([])

  /** 未读通知数量（只读） */
  const getUnreadCount = computed(() => unreadCount.value)

  /** 将通知加入弹出队列，超出上限时移除最旧记录 */
  function addToQueue(notification: Notification) {
    popupQueue.value.push(notification)
    if (popupQueue.value.length > MAX_POPUP_QUEUE) {
      popupQueue.value.shift()
    }
  }

  /** 按 id 从弹出队列中移除指定通知 */
  function removeFromQueue(id: number) {
    popupQueue.value = popupQueue.value.filter(n => n.id !== id)
  }

  /** 设置未读通知数量 */
  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  /** 设置 SSE 连接状态 */
  function setConnected(val: boolean) {
    isConnected.value = val
  }

  /** 清空弹出队列 */
  function clearQueue() {
    popupQueue.value = []
  }

  return {
    unreadCount,
    isConnected,
    popupQueue,
    latestNotifications,
    getUnreadCount,
    addToQueue,
    removeFromQueue,
    setUnreadCount,
    setConnected,
    clearQueue
  }
})
