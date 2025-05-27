import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import GroupReportPrefill from '../views/GroupReportPrefill.vue'
import LoginView from '@/views/LoginView.vue'
import { getCurrentUser } from '@/api/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/about', name: 'about', component: () => import('../views/AboutView.vue') },
    { path: '/group-prefill', name: 'group-prefill', component: GroupReportPrefill, meta: { requiresAuth: true } },
    { path: '/login', name: 'login', component: LoginView }
  ]
})

router.beforeEach(async (to, from, next) => {
  if (!to.meta.requiresAuth) return next()

  const token = localStorage.getItem('token')
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  try {
    await getCurrentUser()
    next()
  } catch {
    console.warn('❌ Invalid or expired token, redirecting to login')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

export default router // ✅ THIS IS MANDATORY
