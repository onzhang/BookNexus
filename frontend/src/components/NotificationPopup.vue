<template>
  <TransitionGroup name="notification-slide" tag="div" class="notification-popup">
    <div
      v-for="notification in visibleNotifications"
      :key="notification.id"
      class="notification-card"
      :class="`type-${notification.type.toLowerCase()}`"
      @click="handleClick(notification.id)"
      @mouseenter="pauseTimer(notification.id)"
      @mouseleave="resumeTimer(notification.id)"
    >
      <!-- 左侧颜色条 -->
      <div class="notification-strip" />

      <!-- 内容区 -->
      <div class="notification-content">
        <!-- 头部：图标 + 标题 + 关闭按钮 -->
        <div class="notification-header">
          <div class="notification-title-row">
            <component :is="typeIcon(notification.type)" class="notification-icon" />
            <span class="notification-title">{{ notification.title }}</span>
          </div>
          <button class="notification-close" @click.stop="handleClose(notification.id)" title="关闭">
            <svg viewBox="0 0 16 16" width="12" height="12">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" fill="none" />
            </svg>
          </button>
        </div>

        <!-- 内容（最多2行） -->
        <p v-if="notification.content" class="notification-text">
          {{ notification.content }}
        </p>

        <!-- 时间 -->
        <span class="notification-time">{{ relativeTime(notification.createdAt) }}</span>
      </div>

      <!-- 自动消失进度条（OVERDUE 不显示） -->
      <div
        v-if="notification.type !== 'OVERDUE'"
        class="notification-progress"
      >
        <div
          class="notification-progress-bar"
          :class="`type-${notification.type.toLowerCase()}`"
          :style="{ width: `${progressMap[notification.id] || 100}%` }"
        />
      </div>
    </div>
  </TransitionGroup>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import type { Notification } from '@/types'
import { Bell, Document, WarningFilled } from '@element-plus/icons-vue'

/* ── Props & Emits ── */
const props = defineProps<{
  notifications: Notification[]
  visible: boolean
}>()

const emit = defineEmits<{
  close: [id: number]
  click: [id: number]
}>()

/* ── 常量 ── */
const MAX_STACK = 4
const AUTO_DISMISS_DELAY: Record<string, number> = {
  SYSTEM: 7000,
  SUBSCRIPTION: 5000,
}

/* ── 状态 ── */
const progressMap = ref<Record<number, number>>({})
const timers = new Map<number, ReturnType<typeof setTimeout>>()
const progressTimers = new Map<number, ReturnType<typeof setInterval>>()
const pausedAt = new Map<number, number>()
const remainingTime = new Map<number, number>()

/* ── 最多显示 4 条（最新的在前） ── */
const visibleNotifications = computed(() =>
  props.notifications.slice(0, MAX_STACK)
)

/* ── 类型图标 ── */
function typeIcon(type: string) {
  switch (type) {
    case 'SYSTEM':
      return Bell
    case 'SUBSCRIPTION':
      return Document
    case 'OVERDUE':
      return WarningFilled
    default:
      return Bell
  }
}

/* ── 相对时间 ── */
function relativeTime(dateStr: string): string {
  const now = Date.now()
  const then = new Date(dateStr).getTime()
  const diff = now - then

  const seconds = Math.floor(diff / 1000)
  if (seconds < 60) return '刚刚'

  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes} 分钟前`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`

  const days = Math.floor(hours / 24)
  if (days < 7) return `${days} 天前`

  return new Date(dateStr).toLocaleDateString('zh-CN')
}

/* ── 自动消失逻辑 ── */
function startAutoDismiss(notification: Notification) {
  if (notification.type === 'OVERDUE') return

  const delay = AUTO_DISMISS_DELAY[notification.type] ?? 5000
  const id = notification.id

  // 清除旧定时器
  clearTimer(id)

  // 进度条
  const startTime = Date.now()
  remainingTime.set(id, delay)

  progressTimers.set(id, setInterval(() => {
    const elapsed = Date.now() - startTime - (pausedAt.get(id) ?? 0)
    const progress = Math.max(0, 100 - (elapsed / delay) * 100)
    progressMap.value[id] = progress
  }, 50))

  // 自动关闭
  timers.set(id, setTimeout(() => {
    handleClose(id)
  }, delay))
}

function clearTimer(id: number) {
  if (timers.has(id)) {
    clearTimeout(timers.get(id)!)
    timers.delete(id)
  }
  if (progressTimers.has(id)) {
    clearInterval(progressTimers.get(id)!)
    progressTimers.delete(id)
  }
  pausedAt.delete(id)
  remainingTime.delete(id)
}

function pauseTimer(id: number) {
  if (timers.has(id)) {
    clearTimeout(timers.get(id)!)
    timers.delete(id)
    pausedAt.set(id, Date.now())
  }
}

function resumeTimer(id: number) {
  const pauseStart = pausedAt.get(id)
  if (!pauseStart) return

  const pauseDuration = Date.now() - pauseStart
  pausedAt.delete(id)

  // 重新计算剩余时间并启动
  const notification = props.notifications.find(n => n.id === id)
  if (!notification || notification.type === 'OVERDUE') return

  const originalDelay = AUTO_DISMISS_DELAY[notification.type] ?? 5000
  const elapsedBeforePause = pauseStart - (remainingTime.get(id) ? Date.now() - (originalDelay - remainingTime.get(id)!) : 0)
  const remaining = originalDelay - elapsedBeforePause - pauseDuration

  if (remaining > 0) {
    timers.set(id, setTimeout(() => {
      handleClose(id)
    }, remaining))
  }
}

/* ── 事件处理 ── */
function handleClose(id: number) {
  clearTimer(id)
  delete progressMap.value[id]
  emit('close', id)
}

function handleClick(id: number) {
  emit('click', id)
}

/* ── 生命周期 ── */
onMounted(() => {
  visibleNotifications.value.forEach(startAutoDismiss)
})

onUnmounted(() => {
  timers.forEach((timer) => clearTimeout(timer))
  timers.clear()
  progressTimers.forEach((timer) => clearInterval(timer))
  progressTimers.clear()
  pausedAt.clear()
  remainingTime.clear()
})
</script>

<style scoped lang="scss">
.notification-popup {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 380px;
  pointer-events: none;
}

/* ── 通知卡片 ── */
.notification-card {
  position: relative;
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  pointer-events: auto;
  cursor: pointer;
  transition: box-shadow var(--transition-fast);

  &:hover {
    box-shadow: var(--shadow-lg);
  }
}

/* 左侧颜色条 */
.notification-strip {
  width: 4px;
  flex-shrink: 0;
}

/* 类型配色 */
.type-system .notification-strip {
  background: var(--primary-color);
}

.type-subscription .notification-strip {
  background: var(--success-color);
}

.type-overdue .notification-strip {
  background: var(--danger-color);
}

/* 内容区 */
.notification-content {
  flex: 1;
  padding: 12px 36px 12px 12px;
  min-width: 0;
}

/* 头部 */
.notification-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 4px;
}

.notification-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.notification-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.type-system .notification-icon {
  color: var(--primary-color);
}

.type-subscription .notification-icon {
  color: var(--success-color);
}

.type-overdue .notification-icon {
  color: var(--danger-color);
}

.notification-title {
  font-weight: 700;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 关闭按钮 */
.notification-close {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--accent-bg);
    color: var(--text-primary);
  }
}

/* 文本内容 */
.notification-text {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.5;
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 时间 */
.notification-time {
  font-size: 11px;
  color: var(--text-secondary);
}

/* 进度条 */
.notification-progress {
  position: absolute;
  bottom: 0;
  left: 4px;
  right: 0;
  height: 3px;
  background: transparent;
}

.notification-progress-bar {
  height: 100%;
  transition: width 50ms linear;
  border-radius: 0 0 0 var(--radius-sm);

  &.type-system {
    background: var(--primary-color);
  }

  &.type-subscription {
    background: var(--success-color);
  }
}

/* ── 滑入/滑出动画 ── */
.notification-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.notification-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.55, 0, 1, 0.45);
}

.notification-slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-slide-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.notification-slide-move {
  transition: transform 0.3s ease;
}
</style>
