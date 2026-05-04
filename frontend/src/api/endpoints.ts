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
  BOOK_DETAIL: (id: number | string) => ({ path: `/v1/public/books/${id}`, method: HttpMethod.GET })
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
  BORROW_MY_PAGE: { path: '/v1/user/borrows', method: HttpMethod.GET }
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
  BORROW_RETURN: (id: number | string) => ({ path: `/v1/admin/borrows/${id}/return`, method: HttpMethod.PUT })
} as const
