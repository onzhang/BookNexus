/**
 * BookNexus — 应用全局状态（Pinia Store）
 *
 * @description 管理应用级的全局 UI 状态，包括侧边栏折叠、全局加载状态等。
 *              与用户无关的通用状态归入此 Store。
 * @author 张俊文
 * @date 2026-05-01
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 应用全局状态 Store */
export const useAppStore = defineStore('app', () => {
  /** 侧边栏是否折叠 */
  const sidebarCollapsed = ref(false)
  /** 全局加载状态 */
  const loading = ref(false)

  /** 切换侧边栏折叠状态 */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /** 设置全局加载状态 */
  function setLoading(val: boolean) {
    loading.value = val
  }

  return {
    sidebarCollapsed,
    loading,
    toggleSidebar,
    setLoading
  }
})
