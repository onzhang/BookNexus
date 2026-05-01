<!--
  ============================================================
  UserLayout.vue — 用户端布局
  @description 普通用户端整体布局组件，固定宽度侧边栏导航、
               顶部操作栏和主内容区 <router-view>。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <el-container class="user-layout">
    <!-- 侧边栏：Logo + 导航菜单 -->
    <el-aside width="200px" class="user-aside">
      <div class="logo" @click="router.push('/user/home')">
        <span class="logo-text">BookNexus</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/user/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/user/borrows">
          <el-icon><Document /></el-icon>
          <template #title>我的借阅</template>
        </el-menu-item>
        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部操作栏：标题 + 用户信息 + 退出 -->
      <el-header class="user-header">
        <span class="header-title">BookNexus 图书管理系统</span>
        <div class="header-right">
          <span class="header-user">{{ userStore.userInfo?.username }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 主内容区：子路由视图 -->
      <el-main class="user-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Document, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

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
.user-layout {
  height: 100vh;
}

.user-aside {
  background-color: var(--sidebar-bg);

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    .logo-text {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  padding: 0 20px;
  height: 60px;

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
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

.user-main {
  background-color: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
}
</style>
