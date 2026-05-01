import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { TOKEN_KEY } from '@/api'

const USER_KEY = 'booknexus_user'

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

const router = createRouter({
  history: createWebHistory(),
  routes
})

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
