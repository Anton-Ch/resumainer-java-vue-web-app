import { createRouter, createWebHistory } from 'vue-router'
import { checkAuthStatus } from '@/services/authService'
import GeneratePlaceholderPage from '@/components/common/GeneratePlaceholderPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/auth',
      name: 'auth',
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
    // Profile sections
    {
      path: '/profile',
      redirect: '/profile/contact'
    },
    {
      path: '/profile/contact',
      name: 'profile-contact',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/experience',
      name: 'profile-experience',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/education',
      name: 'profile-education',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/projects',
      name: 'profile-projects',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/courses',
      name: 'profile-courses',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/additional',
      name: 'profile-additional',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    // Generate resume placeholders (with stepper)
    {
      path: '/generate/vacancy',
      name: 'generate-vacancy',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/generate/settings',
      name: 'generate-settings',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/generate/review',
      name: 'generate-review',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/generate/export',
      name: 'generate-export',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/auth'
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
      next({ path: '/auth', query: { redirect: to.fullPath } })
      return
    }

    // Route is for guests only but user is authenticated
    if (to.matched.some(r => r.meta.requiresGuest) && isAuthenticated) {
      next({ path: '/home' })
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
      next({ path: '/auth' })
    } else {
      next()
    }
  }
})

export default router
