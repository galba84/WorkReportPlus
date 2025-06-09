import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '../views/HomeView.vue'
import NewReport from '../views/NewReport.vue'
import LoginView from '@/views/LoginView.vue'
import {getCurrentUser} from '@/api/auth'
import AdminView from '@/views/AdminView.vue'
import ExportReport from '@/views/ExportReport.vue'
import GroupManager from '@/views/GroupManager.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'home', component: HomeView},
    {path: '/about', name: 'about', component: () => import('../views/AboutView.vue')},
    {path: '/new-report', name: 'new-report', component: NewReport, meta: {requiresAuth: true}},
    {path: '/login', name: 'login', component: LoginView},
    {path: '/admin', name: 'Admin', component: AdminView, meta: { requiresAuth: true } },
    {path: '/export-report', name: 'export-report', component: ExportReport, meta: { requiresAuth: true } },
    {path: '/groups', name: 'groups', component: GroupManager, meta: { requiresAuth: true } },
    {path: '/regions', component: () => import('@/views/RegionManager.vue'),  meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  if (!to.meta.requiresAuth) return next()

  const token = localStorage.getItem('token')
  if (!token) {
    return next({path: '/login', query: {redirect: to.fullPath}})
  }

  try {
    await getCurrentUser()
    next()
  } catch {
    console.warn('❌ Invalid or expired token, redirecting to login')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    next({path: '/login', query: {redirect: to.fullPath}})
  }
})

export default router // ✅ THIS IS MANDATORY
