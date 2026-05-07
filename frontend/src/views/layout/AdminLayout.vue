<!--
  ============================================================
  AdminLayout.vue — 管理后台布局
  @description 管理员端整体布局组件，包含可折叠侧边栏导航、顶部操作栏
               （含用户名展示和退出登录）和主内容区 <router-view>。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <el-container class="admin-layout">
    <!-- 侧边栏：Logo + 导航菜单（可折叠） -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
      <div class="logo" @click="router.push('/admin/dashboard')">
        <el-icon :size="24" color="#fff"><Reading /></el-icon>
        <span v-show="!isCollapse" class="logo-text">BookNexus</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#4A3728"
        text-color="#D4C5B2"
        active-text-color="#C4956A"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/books">
          <el-icon><Reading /></el-icon>
          <template #title>书籍管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/bookshelves">
          <el-icon><Collection /></el-icon>
          <template #title>书架管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><FolderOpened /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/borrows">
          <el-icon><Document /></el-icon>
          <template #title>借阅管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/messages">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>留言管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部操作栏：折叠按钮 + 标题 + 用户信息 + 退出 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-button
            :icon="isCollapse ? Expand : Fold"
            text
            @click="isCollapse = !isCollapse"
          />
          <span class="header-title">BookNexus 管理后台</span>
        </div>
        <div class="header-right">
          <span class="header-user">{{ userStore.userInfo?.username }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 主内容区：子路由视图 -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Odometer,
  User,
  Reading,
  Document,
  Expand,
  Fold,
  Collection,
  FolderOpened,
  Bell,
  ChatDotRound
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 侧边栏是否折叠 */
const isCollapse = ref(false)

/** 当前激活的菜单项路径 */
const activeMenu = computed(() => route.path)

/**
 * 退出登录
 * @description 弹出确认对话框，确认后清除用户状态并跳转登录页
 */
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #4A3728;
  overflow: hidden;
  transition: width 0.3s;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .logo-img {
      width: 32px;
      height: 32px;
      flex-shrink: 0;
    }

    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 700;
      color: #fff;
      white-space: nowrap;
      font-family: 'Playfair Display', 'Noto Serif SC', serif;
      letter-spacing: 1px;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  padding: 0 20px;
  height: 60px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-user {
      color: var(--text-regular);
    }
  }
}

.admin-main {
  background-color: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
}
</style>
