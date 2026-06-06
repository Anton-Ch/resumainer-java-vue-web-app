import { createRouter, createWebHashHistory } from 'vue-router'
import HomeReady from './views/HomeReady.vue'
import HomeIncomplete from './views/HomeIncomplete.vue'
import HomeEmpty from './views/HomeEmpty.vue'
import HomeLoading from './views/HomeLoading.vue'
import GeneratePlaceholder from './views/GeneratePlaceholder.vue'
import PlaceholderPage from './views/PlaceholderPage.vue'

// Demo routes are for OpenDesign prototype review only.
// Production routes must not use /demo/...
// Production User Home is /app/home
export const routes = [
  { path: '/', redirect: '/demo/home-ready' },
  { path: '/demo/home-ready', component: HomeReady, props: { resumes: 'ready' } },
  { path: '/demo/home-incomplete', component: HomeIncomplete, props: { resumes: 'empty' } },
  { path: '/demo/home-empty', component: HomeEmpty, props: { resumes: 'empty' } },
  { path: '/demo/home-loading', component: HomeLoading, props: { resumes: 'empty', loading: true } },
  // Placeholder pages — keep as production-like routes for now
  { path: '/profile/contact', component: PlaceholderPage },
  { path: '/profile/experience', component: PlaceholderPage },
  { path: '/profile/education', component: PlaceholderPage },
  { path: '/profile/projects', component: PlaceholderPage },
  { path: '/profile/courses', component: PlaceholderPage },
  { path: '/profile/additional', component: PlaceholderPage },
  { path: '/generate/vacancy', component: GeneratePlaceholder },
  { path: '/generate/settings', component: GeneratePlaceholder },
  { path: '/generate/review', component: GeneratePlaceholder },
  { path: '/generate/export', component: GeneratePlaceholder },
  { path: '/admin', component: PlaceholderPage }
]

const router = createRouter({ history: createWebHashHistory(), routes })
export default router
