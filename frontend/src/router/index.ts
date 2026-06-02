import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/AuthPage.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/AuthPage.vue')
    },
    {
      path: '/home',
      name: 'user-home',
      component: () => import('../views/UserHomePage.vue')
    },
    {
      path: '/admin',
      name: 'admin-home',
      component: () => import('../views/AdminHomePage.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

export default router
