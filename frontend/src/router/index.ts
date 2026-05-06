/**
 * BookNexus — Vue Router 路由配置
 *
 * @description 定义应用所有路由规则，包括根路径自动重定向（根据角色跳转）、
 *              用户端/管理端分栏布局路由、路由守卫（Token 校验 + 标题设置）。
 * @author 张俊文
 * @date 2026-05-01
 */

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { TOKEN_KEY } from '@/api'

/** localStorage 中存储用户信息的键名 */
const USER_KEY = 'booknexus_user'

/** 路由表 */
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => {
      const token = localStorage.getItem(TOKEN_KEY)
      if (!token) return '/login'
      try {
        const user = JSON.parse(localStorage.getItem(USER_KEY) || 'null')
        if (user?.role === 'ADMIN') return '/admin/dashboard'
        return '/user/home'
      } catch {
        return '/user/home'
      }
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/user',
    component: () => import('@/views/layout/UserLayout.vue'),
    redirect: '/user/home',
    meta: { title: '用户端' },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'borrows',
        name: 'MyBorrows',
        component: () => import('@/views/user/MyBorrows.vue'),
        meta: { title: '我的借阅' }
      },
      {
        path: 'books/:id',
        name: 'BookDetail',
        component: () => import('@/views/user/BookDetail.vue'),
        meta: { title: '书籍详情' }
      },
      {
        path: 'favorites',
        name: 'MyFavorites',
        component: () => import('@/views/user/Favorites.vue'),
        meta: { title: '我的收藏' }
      },
      {
        path: 'notifications',
        name: 'MyNotifications',
        component: () => import('@/views/user/Notifications.vue'),
        meta: { title: '我的通知' }
      },
      {
        path: 'messages',
        name: 'MyMessages',
        component: () => import('@/views/user/Messages.vue'),
        meta: { title: '留言建议' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { title: '管理后台' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'books',
        name: 'AdminBooks',
        component: () => import('@/views/admin/Books.vue'),
        meta: { title: '书籍管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'borrows',
        name: 'AdminBorrows',
        component: () => import('@/views/admin/Borrows.vue'),
        meta: { title: '借阅管理' }
      },
      {
        path: 'bookshelves',
        name: 'AdminBookshelves',
        component: () => import('@/views/admin/Bookshelves.vue'),
        meta: { title: '书架管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/Announcements.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('@/views/admin/Messages.vue'),
        meta: { title: '留言管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

/** Vue Router 实例 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置守卫
 * @description 设置页面标题；未登录且非登录页时自动跳转 /login
 */
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'BookNexus'} - 图书管理系统`

  const token = localStorage.getItem(TOKEN_KEY)
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
