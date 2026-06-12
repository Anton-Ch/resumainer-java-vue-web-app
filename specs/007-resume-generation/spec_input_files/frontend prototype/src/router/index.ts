import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/generate/vacancy'
    },
    {
      path: '/generate/vacancy',
      name: 'generate-vacancy',
      component: () => import('@/views/generate/GenerateVacancyPage.vue')
    },
    {
      path: '/generate/settings',
      name: 'generate-settings',
      component: () => import('@/views/generate/GenerateSettingsPage.vue')
    },
    {
      path: '/generate/review',
      name: 'generate-review',
      component: () => import('@/views/generate/GenerateReviewPage.vue')
    },
    {
      path: '/generate/export',
      name: 'generate-export',
      component: () => import('@/views/generate/GenerateExportPage.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/generate/vacancy'
    }
  ]
})

export default router
