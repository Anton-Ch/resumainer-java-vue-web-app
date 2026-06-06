import { createRouter, createWebHistory } from 'vue-router'
import { checkAuthStatus } from '@/services/authService'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/app/auth',
      name: 'auth',
      component: () => import('@/views/AuthPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/app/home',
      name: 'user-home',
      component: () => import('@/views/UserHomePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/admin',
      name: 'admin-home',
      component: () => import('@/views/AdminHomePage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    // Profile section placeholders
    {
      path: '/app/profile/contact',
      name: 'profile-contact',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/profile/experience',
      name: 'profile-experience',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/profile/education',
      name: 'profile-education',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/profile/projects',
      name: 'profile-projects',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/profile/courses',
      name: 'profile-courses',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/profile/additional',
      name: 'profile-additional',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    // Generate resume placeholders
    {
      path: '/app/generate/vacancy',
      name: 'generate-vacancy',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/generate/settings',
      name: 'generate-settings',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/generate/review',
      name: 'generate-review',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/app/generate/export',
      name: 'generate-export',
      component: () => import('@/components/common/PlaceholderPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/app/auth'
    }
  ]
})

/**
 * Global navigation guard.
 *
 * - Protected routes (requiresAuth): redirect to /app/auth if not authenticated.
 * - Auth routes (requiresGuest): redirect to /app/home if already authenticated.
 * - Admin routes (requiresAdmin): redirect to /app/home if role is not ADMIN.
 */
router.beforeEach(async (to, from, next) => {
  try {
    const status = await checkAuthStatus()
    const isAuthenticated = status.authenticated
    const role = status.role

    // Route requires auth but user is not authenticated
    if (to.matched.some(r => r.meta.requiresAuth) && !isAuthenticated) {
      next({ path: '/app/auth', query: { redirect: to.fullPath } })
      return
    }

    // Route is for guests only but user is authenticated
    if (to.matched.some(r => r.meta.requiresGuest) && isAuthenticated) {
      next({ path: '/app/home' })
      return
    }

    // Route requires ADMIN role but user is not admin
    if (to.matched.some(r => r.meta.requiresAdmin) && role !== 'ADMIN') {
      next({ path: '/app/home' })
      return
    }

    next()
  } catch {
    // Auth check failed (network error, etc.) — treat as not authenticated
    if (to.matched.some(r => r.meta.requiresAuth)) {
      next({ path: '/app/auth' })
    } else {
      next()
    }
  }
})

export default router
