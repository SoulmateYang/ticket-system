import { createRouter, createWebHistory } from 'vue-router'
import { authApi } from '../api'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'tickets',
        name: 'TicketList',
        component: () => import('../views/TicketList.vue'),
        meta: { title: '票务管理' }
      },
      {
        path: 'tickets/passes',
        name: 'PassList',
        component: () => import('../views/PassList.vue'),
        meta: { title: '年/月卡管理' }
      },
      {
        path: 'ota/orders',
        name: 'OtaOrders',
        component: () => import('../views/OtaOrders.vue'),
        meta: { title: 'OTA订单' }
      },
      {
        path: 'logs/entries',
        name: 'EntryLogs',
        component: () => import('../views/EntryLogs.vue'),
        meta: { title: '入园记录' }
      },
      {
        path: 'staff',
        name: 'Staff',
        component: () => import('../views/Staff.vue'),
        meta: { title: '员工管理', roles: ['ADMIN'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '管理后台'} - 景区票务中台`

  if (to.meta.public) {
    next()
    return
  }

  const token = authApi.getToken()
  if (!token) {
    next('/login')
    return
  }

  const staff = authApi.getStaff()
  if (to.meta.roles && staff) {
    const hasRole = to.meta.roles.includes(staff.role)
    if (!hasRole) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router