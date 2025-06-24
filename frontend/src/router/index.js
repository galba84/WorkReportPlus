import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import NewReport from '../views/NewReport.vue'
import LoginView from '@/views/LoginView.vue'
import { getCurrentUser } from '@/api/auth'
import AdminView from '@/views/AdminView.vue'
import ExportReport from '@/views/ExportReport.vue'
import SearchReport from '@/views/SearchReportsView.vue'
import GroupManager from '@/views/GroupManager.vue'
import RegionReportTemplate from '@/views/RegionReportTemplate.vue'
import UsersView from '@/views/UsersView.vue'
import AuditLog from '@/views/AuditLogView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/about', name: 'about', component: () => import('../views/AboutView.vue') },
    { path: '/new-report', name: 'new-report', component: NewReport, meta: { requiresAuth: true } },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/admin', component: AdminView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
    { path: '/export-report', name: 'export-report', component: ExportReport, meta: { requiresAuth: true } },
    { path: '/search-report', name: 'search-report', component: SearchReport, meta: { requiresAuth: true } },
    { path: '/groups', name: 'groups', component: GroupManager, meta: { requiresAuth: true } },
    { path: '/regions', component: () => import('@/views/RegionManager.vue'), meta: { requiresAuth: true } },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { requiresAuth: false } },
    { path: '/RegionReportTemplate', component: RegionReportTemplate, meta: { requiresAuth: true } },
    { path: '/audit-logs', component: AuditLog, meta: { requiresAuth: true } },
    {
      path: '/users',
      name: 'users',
      component: UsersView,
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  // if no auth needed, just proceed
  if (!to.meta.requiresAuth) return next()

  const token = localStorage.getItem('token')
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  let user
  try {
    // fetch and store current user (including role) in localStorage
    user = await getCurrentUser()
    localStorage.setItem('user', JSON.stringify(user))
  } catch {
    console.warn('❌ Invalid or expired token, redirecting to login')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // check role-based access
  if (to.meta.roles) {
    const { role } = user
    if (!to.meta.roles.includes(role)) {
      // not authorized for this route
      return next({ path: '/' })
    }
  }

  next()
})

export default router
