<template>
  <header class="app-header">
    <div class="header-inner">
      <div class="header-left">
        <router-link to="/app/home" class="header-logo-link">
          <span class="header-brand">ResumAIner</span>
        </router-link>
        <nav class="header-nav">
          <router-link to="/app/home" class="nav-item" active-class="nav-item-active">
            {{ $t('nav.home') }}
          </router-link>
          <router-link to="/app/profile/contact" class="nav-item" active-class="nav-item-active">
            {{ $t('nav.myProfile') }}
          </router-link>
          <router-link to="/app/generate/vacancy" class="nav-item" active-class="nav-item-active">
            {{ $t('nav.generateResume') }}
          </router-link>
          <router-link v-if="isAdmin" to="/app/admin" class="nav-item" active-class="nav-item-active">
            {{ $t('nav.admin') }}
          </router-link>
        </nav>
      </div>
      <div class="header-right">
        <LanguageSwitcher />
        <Button
          icon="pi pi-sign-out"
          class="p-button-text p-button-sm logout-btn"
          :label="$t('nav.logout')"
          @click="handleLogout"
          :disabled="loggingOut"
          v-tooltip.top="$t('nav.logout')"
          :aria-label="$t('nav.logout')"
        />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuth } from '@/composables/useAuth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import Button from 'primevue/button'

const router = useRouter()
const { t } = useI18n()
const { role, logout } = useAuth()
const loggingOut = ref(false)

const isAdmin = computed(() => role.value === 'ADMIN')

async function handleLogout() {
  loggingOut.value = true
  try {
    await logout()
    router.push('/app/auth')
  } finally {
    loggingOut.value = false
  }
}
</script>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  height: 56px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}
.header-logo-link {
  text-decoration: none;
}
.header-brand {
  font-family: 'Manrope', sans-serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: #10233F;
  letter-spacing: -0.3px;
}
.header-nav {
  display: flex;
  gap: 0.25rem;
}
.nav-item {
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #5D718B;
  text-decoration: none;
  border-radius: 6px;
  transition: background 0.15s, color 0.15s;
}
.nav-item:hover {
  background: #F3F4F6;
  color: #10233F;
}
.nav-item-active {
  background: #EFF6FF;
  color: #2F6BFF;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.logout-btn {
  color: #5D718B;
}
.logout-btn:hover {
  color: #C2410C;
}
</style>
