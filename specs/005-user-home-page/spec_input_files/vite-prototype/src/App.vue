<template>
  <div id="app-root" v-cloak>
    <nav class="resumainer-nav">
      <div class="nav-logo" @click="goHome">
        <img :src="logoSrc" alt="ResumAIner">
      </div>
      <div class="nav-links-desktop">
        <router-link v-for="item in navItems" :key="item.route" :to="item.route">
          {{ t(item.label) }}
        </router-link>
      </div>
      <div class="nav-right">
        <button class="hamburger-btn" @click="mobileMenuOpen = !mobileMenuOpen" :aria-label="t('nav.mobileMenu')">
          <i :class="['pi', mobileMenuOpen ? 'pi-times' : 'pi-bars']"></i>
        </button>
        <button :class="['lang-btn', { active: lang === 'en' }]" @click="setLang('en')" :aria-label="t('nav.language') + ' EN'">EN</button>
        <button :class="['lang-btn', { active: lang === 'ru' }]" @click="setLang('ru')" :aria-label="t('nav.language') + ' RU'">RU</button>
        <button class="logout-btn" v-tooltip.top="t('nav.logout')" :aria-label="t('nav.logout')" @click="onLogout">
          <i class="pi pi-sign-out"></i>
        </button>
      </div>
    </nav>
    <div class="mobile-menu-overlay" v-if="mobileMenuOpen" @click="mobileMenuOpen = false">
      <div class="mobile-menu-panel" @click.stop>
        <router-link v-for="item in navItems" :key="item.route" :to="item.route" @click="mobileMenuOpen = false">
          <i :class="['pi', getNavIcon(item.route)]"></i>
          {{ t(item.label) }}
        </router-link>
      </div>
    </div>

    <div class="main-content">
      <router-view :key="$route.path" />
    </div>

    <!--
      TEMPORARY PROTOTYPE FOOTER — OpenDesign demo only.
      This footer must not be included in production implementation.
      Demo routes /demo/... are for prototype review only.
      Real production implementation must use one User Home route under /app/home
      and determine state based on API data.
    -->
    <div class="prototype-footer">
      <div class="footer-label">{{ t('prototypeFooter.label') }}</div>
      <div class="footer-links">
        <router-link to="/demo/home-ready">{{ t('prototypeFooter.ready') }}</router-link>
        <router-link to="/demo/home-incomplete">{{ t('prototypeFooter.incomplete') }}</router-link>
        <router-link to="/demo/home-empty">{{ t('prototypeFooter.empty') }}</router-link>
        <router-link to="/demo/home-loading">{{ t('prototypeFooter.loading') }}</router-link>
      </div>
      <div class="footer-note">{{ t('prototypeFooter.note') }}</div>
    </div>

    <ResumeDetailsDialog v-model="showDetailsModal" :resume="selectedResume" @delete="confirmDelete" />

    <Teleport to="body">
      <div class="delete-dialog-mask" v-if="showDeleteDialog" @click.self="showDeleteDialog = false">
        <div class="delete-dialog">
          <h3>{{ t('deleteResume.title') }}</h3>
          <p>{{ t('deleteResume.text') }}</p>
          <div class="dialog-actions">
            <p-button :label="t('deleteResume.cancel')" class="p-button-text" @click="showDeleteDialog = false" />
            <p-button :label="t('deleteResume.confirm')" icon="pi pi-trash" class="p-button-danger" @click="doDelete" />
          </div>
        </div>
      </div>
    </Teleport>

    <div class="toast-container" v-if="toasts.length">
      <div v-for="(toast, i) in toasts" :key="i" :class="['toast-item', toast.severity === 'error' ? 'error' : '']">
        <i :class="['pi', toast.severity === 'success' ? 'pi-check-circle' : 'pi-exclamation-circle']" :style="{ color: toast.severity === 'success' ? 'var(--accent)' : 'var(--error)' }" />
        <span>{{ toast.detail }}</span>
        <button class="toast-close" @click="toasts.splice(i,1)"><i class="pi pi-times" /></button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, provide, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import messages from './i18n/messages.js'
import { mockResumes } from './data/mockResumes.js'
import ResumeDetailsDialog from './components/ResumeDetailsDialog.vue'

const router = useRouter()
const logoSrc = '/mpvzg80w-resumainer-full.svg'

const lang = ref('en')
const mobileMenuOpen = ref(false)
const showDetailsModal = ref(false)
const selectedResume = ref(null)
const showDeleteDialog = ref(false)
const resumeToDelete = ref(null)
const toasts = ref([])
const coverLetterOpen = ref(false)
const isMobile = ref(window.innerWidth < 640)

const t = (key) => messages[lang.value]?.[key] || key
const setLang = (l) => { lang.value = l }

const navItems = [
  { label: 'nav.home', route: '/demo/home-ready' },
  { label: 'nav.myProfile', route: '/profile/contact' },
  { label: 'nav.generateResume', route: '/generate/vacancy' },
  { label: 'nav.admin', route: '/admin' }
]

const goHome = () => { router.push('/demo/home-ready'); mobileMenuOpen.value = false }

const getNavIcon = (route) => {
  if (route.startsWith('/demo/home')) return 'pi-home'
  if (route.startsWith('/profile')) return 'pi-user'
  if (route.startsWith('/generate')) return 'pi-file-plus'
  if (route.startsWith('/admin')) return 'pi-shield'
  return 'pi-circle'
}

const onLogout = () => {
  showToast('info', lang.value === 'ru' ? 'До встречи!' : 'See you soon!')
}

const showToast = (severity, detail) => {
  toasts.value.push({ severity, detail })
  setTimeout(() => { toasts.value.shift() }, 4000)
}

const openResumeModal = (resume) => {
  selectedResume.value = resume
  coverLetterOpen.value = false
  showDetailsModal.value = true
}

const confirmDelete = (resume) => {
  resumeToDelete.value = resume
  showDeleteDialog.value = true
}

const doDelete = () => {
  if (!resumeToDelete.value) return
  const idx = mockResumes.findIndex(r => r.id === resumeToDelete.value.id)
  if (idx !== -1) mockResumes.splice(idx, 1)
  showDeleteDialog.value = false
  showDetailsModal.value = false
  selectedResume.value = null
  resumeToDelete.value = null
  showToast('success', t('deleteResume.success'))
}

let resizeHandler
onMounted(() => {
  resizeHandler = () => { isMobile.value = window.innerWidth < 640 }
  window.addEventListener('resize', resizeHandler)
})
onUnmounted(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})

provide('t', t)
provide('lang', lang)
provide('setLang', setLang)
provide('isMobile', isMobile)
provide('openResumeModal', openResumeModal)
provide('showToast', showToast)
provide('goHome', goHome)
provide('onLogout', onLogout)
provide('navItems', navItems)
provide('confirmDelete', confirmDelete)
provide('doDelete', doDelete)
</script>
