import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

// 角色标识：1=超级管理员(ADMIN) 2=销售员(SALES) 3=库存管理员(STOCK)
export const ROLES = {
  ADMIN: 1,
  SALES: 2,
  STOCK: 3
}

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据概览', roles: [ROLES.ADMIN, ROLES.SALES, ROLES.STOCK] }
      },
      {
        path: 'category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '商品分类', roles: [ROLES.ADMIN] }
      },
      {
        path: 'product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '商品管理', roles: [ROLES.ADMIN] }
      },
      {
        path: 'customer',
        component: () => import('@/views/customer/index.vue'),
        meta: { title: '客户管理', roles: [ROLES.ADMIN] }
      },
      {
        path: 'employee',
        component: () => import('@/views/employee/index.vue'),
        meta: { title: '员工管理', roles: [ROLES.ADMIN] }
      },
      {
        path: 'sales/order',
        component: () => import('@/views/sales/order/index.vue'),
        meta: { title: '销售订单', roles: [ROLES.ADMIN, ROLES.SALES] }
      },
      {
        path: 'sales/order/create',
        component: () => import('@/views/sales/order/create.vue'),
        meta: { title: '新建订单', roles: [ROLES.ADMIN, ROLES.SALES] }
      },
      {
        path: 'sales/order/detail/:id',
        component: () => import('@/views/sales/order/detail.vue'),
        meta: { title: '订单详情', roles: [ROLES.ADMIN, ROLES.SALES] }
      },
      {
        path: 'stock',
        component: () => import('@/views/stock/index.vue'),
        meta: { title: '库存管理', roles: [ROLES.ADMIN, ROLES.STOCK] }
      },
      {
        path: 'analysis/trend',
        component: () => import('@/views/analysis/trend.vue'),
        meta: { title: '销售趋势', roles: [ROLES.ADMIN] }
      },
      {
        path: 'analysis/product-rank',
        component: () => import('@/views/analysis/product-rank.vue'),
        meta: { title: '商品排行', roles: [ROLES.ADMIN] }
      },
      {
        path: 'analysis/employee',
        component: () => import('@/views/analysis/employee.vue'),
        meta: { title: '员工业绩', roles: [ROLES.ADMIN] }
      },
      {
        path: 'profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人资料', roles: [ROLES.ADMIN, ROLES.SALES, ROLES.STOCK] }
      },
      {
        path: 'profile/password',
        component: () => import('@/views/profile/password.vue'),
        meta: { title: '修改密码', roles: [ROLES.ADMIN, ROLES.SALES, ROLES.STOCK] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 角色校验：读取 userInfo 中的 roleId
  let roleId = null
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    roleId = Number(userInfo.roleId)
  } catch (e) {
    roleId = null
  }

  // 目标路由声明了角色限制，且当前角色不在允许列表内 → 拦截
  const allowedRoles = to.meta?.roles
  if (allowedRoles && Array.isArray(allowedRoles) && allowedRoles.length > 0) {
    if (!roleId || !allowedRoles.includes(roleId)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
