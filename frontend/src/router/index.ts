import { createRouter, createWebHistory } from 'vue-router'
import { checkAuthStatus } from '@/services/authService'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/AuthPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/AuthPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/home',
      name: 'user-home',
      component: () => import('@/views/UserHomePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin-home',
      component: () => import('@/views/AdminHomePage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

/**
 * Global navigation guard.
 *
 * - Protected routes (requiresAuth): redirect to /login if not authenticated.
 * - Auth routes (requiresGuest): redirect to /home if already authenticated.
 * - Admin routes (requiresAdmin): redirect to /home if role is not ADMIN.
 */
router.beforeEach(async (to, from, next) => {
  try {
    const status = await checkAuthStatus()
    const isAuthenticated = status.authenticated
    const role = status.role

    // Route requires auth but user is not authenticated
    if (to.matched.some(r => r.meta.requiresAuth) && !isAuthenticated) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // Route is for guests only but user is authenticated
    if (to.matched.some(r => r.meta.requiresGuest) && isAuthenticated) {
      next({ path: role === 'ADMIN' ? '/admin' : '/home' })
      return
    }

    // Route requires ADMIN role but user is not admin
    if (to.matched.some(r => r.meta.requiresAdmin) && role !== 'ADMIN') {
      next({ path: '/home' })
      return
    }

    next()
  } catch {
    // Auth check failed (network error, etc.) — treat as not authenticated
    if (to.matched.some(r => r.meta.requiresAuth)) {
      next({ path: '/login' })
    } else {
      next()
    }
  }
})

export default router
