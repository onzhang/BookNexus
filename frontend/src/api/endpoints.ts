/**
 * BookNexus — 统一 API 路由常量
 *
 * @description 集中定义所有后端 API 路径和 HTTP 方法，杜绝硬编码散布在各组件中。
 *              与后端 Controller 的 {@code @RequestMapping/@GetMapping/@PostMapping} 一一对应。
 *              新增接口时需同时在此文件中登记，防止前后端路由不一致。
 *
 *              路径格式: /v1/{scope}/{resource} 或 /v1/{scope}/{resource}/{id}
 *                          ↑ 不含 /api 前缀（由 Axios baseURL 统一添加）
 *
 * @author 张俊文
 * @date 2026-05-04
 */

/** HTTP 方法枚举 */
export const HttpMethod = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE'
} as const

/** ==================== 公开接口（无需登录） ==================== */

export const PublicAPI = {
  /** 用户注册 */
  AUTH_REGISTER: { path: '/v1/public/auth/register', method: HttpMethod.POST },
  /** 用户登录 */
  AUTH_LOGIN: { path: '/v1/public/auth/login', method: HttpMethod.POST },
  /** 刷新令牌 */
  AUTH_REFRESH: { path: '/v1/public/auth/refresh', method: HttpMethod.POST },
  /** 图书分页搜索 */
  BOOK_PAGE: { path: '/v1/public/books', method: HttpMethod.GET },
  /** 图书详情 */
  BOOK_DETAIL: (id: number | string) => ({ path: `/v1/public/books/${id}`, method: HttpMethod.GET }),
  /** 书架列表（公开） */
  BOOKSHELF_LIST: { path: '/v1/public/bookshelves', method: HttpMethod.GET },
  /** 分类列表（公开） */
  CATEGORY_LIST: { path: '/v1/public/categories', method: HttpMethod.GET },
  /** 分类树形结构（公开） */
  CATEGORY_TREE: { path: '/v1/public/categories/tree', method: HttpMethod.GET },

  // -- 公告（公开） --
  /** 公告分页查询 */
  ANNOUNCEMENT_PAGE: { path: '/v1/public/announcements', method: HttpMethod.GET },
  /** 公告详情 */
  ANNOUNCEMENT_DETAIL: (id: number | string) => ({ path: `/v1/public/announcements/${id}`, method: HttpMethod.GET })
} as const

/** ==================== 用户端接口（需登录） ==================== */

export const UserAPI = {
  /** 获取当前用户信息 */
  AUTH_ME: { path: '/v1/user/auth/me', method: HttpMethod.GET },
  /** 借阅图书 */
  BORROW_CREATE: { path: '/v1/user/borrows', method: HttpMethod.POST },
  /** 归还图书 */
  BORROW_RETURN: (id: number | string) => ({ path: `/v1/user/borrows/${id}/return`, method: HttpMethod.PUT }),
  /** 续借图书 */
  BORROW_RENEW: (id: number | string) => ({ path: `/v1/user/borrows/${id}/renew`, method: HttpMethod.PUT }),
  /** 我的借阅记录 */
  BORROW_MY_PAGE: { path: '/v1/user/borrows', method: HttpMethod.GET },

  // -- 收藏管理 --
  /** 收藏图书 */
  FAVORITE_CREATE: { path: '/v1/user/favorites', method: HttpMethod.POST },
  /** 取消收藏 */
  FAVORITE_DELETE: (bookId: number | string) => ({ path: `/v1/user/favorites/${bookId}`, method: HttpMethod.DELETE }),
  /** 我的收藏列表 */
  FAVORITE_MY_PAGE: { path: '/v1/user/favorites', method: HttpMethod.GET },
  /** 检查是否已收藏 */
  FAVORITE_CHECK: (bookId: number | string) => ({ path: `/v1/user/favorites/check/${bookId}`, method: HttpMethod.GET }),

  // -- 订阅管理 --
  /** 订阅图书 */
  SUBSCRIPTION_CREATE: { path: '/v1/user/subscriptions', method: HttpMethod.POST },
  /** 取消订阅 */
  SUBSCRIPTION_DELETE: (bookId: number | string) => ({ path: `/v1/user/subscriptions/${bookId}`, method: HttpMethod.DELETE }),
  /** 我的订阅列表 */
  SUBSCRIPTION_MY_PAGE: { path: '/v1/user/subscriptions', method: HttpMethod.GET },
  /** 检查是否已订阅 */
  SUBSCRIPTION_CHECK: (bookId: number | string) => ({ path: `/v1/user/subscriptions/check/${bookId}`, method: HttpMethod.GET }),

  // -- 通知管理 --
  /** 我的通知列表 */
  NOTIFICATION_PAGE: { path: '/v1/user/notifications', method: HttpMethod.GET },
  /** 未读通知数量 */
  NOTIFICATION_UNREAD_COUNT: { path: '/v1/user/notifications/unread-count', method: HttpMethod.GET },
  /** 标记通知已读 */
  NOTIFICATION_READ: (id: number | string) => ({ path: `/v1/user/notifications/${id}/read`, method: HttpMethod.PUT }),

  // -- 留言管理 --
  /** 提交留言 */
  MESSAGE_CREATE: { path: '/v1/user/messages', method: HttpMethod.POST },
  /** 我的留言列表 */
  MESSAGE_MY_PAGE: { path: '/v1/user/messages', method: HttpMethod.GET },

  // -- 个人资料 --
  /** 更新个人资料 */
  PROFILE_UPDATE: { path: '/v1/user/profile', method: HttpMethod.PUT },
  /** 上传头像 */
  PROFILE_AVATAR: { path: '/v1/user/profile/avatar', method: HttpMethod.POST }
} as const

/** ==================== 管理端接口（需 ADMIN 角色） ==================== */

export const AdminAPI = {
  // -- 图书管理 --
  /** 图书分页查询 */
  BOOK_PAGE: { path: '/v1/admin/books', method: HttpMethod.GET },
  /** 新增图书 */
  BOOK_CREATE: { path: '/v1/admin/books', method: HttpMethod.POST },
  /** 编辑图书 */
  BOOK_UPDATE: (id: number | string) => ({ path: `/v1/admin/books/${id}`, method: HttpMethod.PUT }),
  /** 删除图书 */
  BOOK_DELETE: (id: number | string) => ({ path: `/v1/admin/books/${id}`, method: HttpMethod.DELETE }),

  // -- 用户管理 --
  /** 用户分页查询 */
  USER_PAGE: { path: '/v1/admin/users', method: HttpMethod.GET },
  /** 用户详情 */
  USER_DETAIL: (id: number | string) => ({ path: `/v1/admin/users/${id}`, method: HttpMethod.GET }),
  /** 编辑用户 */
  USER_UPDATE: (id: number | string) => ({ path: `/v1/admin/users/${id}`, method: HttpMethod.PUT }),
  /** 启用/禁用用户 */
  USER_STATUS: (id: number | string) => ({ path: `/v1/admin/users/${id}/status`, method: HttpMethod.PUT }),

  // -- 借阅管理 --
  /** 借阅记录分页查询 */
  BORROW_PAGE: { path: '/v1/admin/borrows', method: HttpMethod.GET },
  /** 强制归还 */
  BORROW_RETURN: (id: number | string) => ({ path: `/v1/admin/borrows/${id}/return`, method: HttpMethod.PUT }),

  // -- 书架管理 --
  /** 书架分页查询 */
  BOOKSHELF_PAGE: { path: '/v1/admin/bookshelves', method: HttpMethod.GET },
  /** 书架列表（全部） */
  BOOKSHELF_ALL: { path: '/v1/admin/bookshelves/all', method: HttpMethod.GET },
  /** 新增书架 */
  BOOKSHELF_CREATE: { path: '/v1/admin/bookshelves', method: HttpMethod.POST },
  /** 编辑书架 */
  BOOKSHELF_UPDATE: (id: number | string) => ({ path: `/v1/admin/bookshelves/${id}`, method: HttpMethod.PUT }),
  /** 删除书架 */
  BOOKSHELF_DELETE: (id: number | string) => ({ path: `/v1/admin/bookshelves/${id}`, method: HttpMethod.DELETE }),

  // -- 分类管理 --
  /** 分类分页查询 */
  CATEGORY_PAGE: { path: '/v1/admin/categories', method: HttpMethod.GET },
  /** 分类列表（全部） */
  CATEGORY_ALL: { path: '/v1/admin/categories/all', method: HttpMethod.GET },
  /** 分类树形结构 */
  CATEGORY_TREE_ADMIN: { path: '/v1/admin/categories/tree', method: HttpMethod.GET },
  /** 新增分类 */
  CATEGORY_CREATE: { path: '/v1/admin/categories', method: HttpMethod.POST },
  /** 编辑分类 */
  CATEGORY_UPDATE: (id: number | string) => ({ path: `/v1/admin/categories/${id}`, method: HttpMethod.PUT }),
  /** 删除分类 */
  CATEGORY_DELETE: (id: number | string) => ({ path: `/v1/admin/categories/${id}`, method: HttpMethod.DELETE }),

  // -- 公告管理 --
  /** 公告分页查询 */
  ANNOUNCEMENT_PAGE: { path: '/v1/admin/announcements', method: HttpMethod.GET },
  /** 新增公告 */
  ANNOUNCEMENT_CREATE: { path: '/v1/admin/announcements', method: HttpMethod.POST },
  /** 编辑公告 */
  ANNOUNCEMENT_UPDATE: (id: number | string) => ({ path: `/v1/admin/announcements/${id}`, method: HttpMethod.PUT }),
  /** 删除公告 */
  ANNOUNCEMENT_DELETE: (id: number | string) => ({ path: `/v1/admin/announcements/${id}`, method: HttpMethod.DELETE }),

  // -- 留言管理 --
  /** 留言分页查询（全部） */
  MESSAGE_PAGE: { path: '/v1/admin/messages', method: HttpMethod.GET },
  /** 回复留言 */
  MESSAGE_REPLY: (id: number | string) => ({ path: `/v1/admin/messages/${id}/reply`, method: HttpMethod.PUT })
} as const
