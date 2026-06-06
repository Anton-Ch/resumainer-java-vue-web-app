<template>
  <header class="resumainer-nav">
    <div class="nav-logo" @click="$router.push('/home')">
      <svg width="32" height="32" viewBox="0 0 32 32" fill="none" shape-rendering="geometricPrecision">
        <rect x="5" y="3.5" width="22" height="25" rx="4" fill="#FFFDF8" stroke="#17211D" stroke-width="2.4"/>
        <circle cx="16" cy="12.2" r="5.8" fill="none" stroke="#17211D" stroke-width="1.9"/>
        <circle cx="16" cy="12.2" r="2.8" fill="#0F8A6A"/>
        <rect x="9.2" y="24" width="13.6" height="3" rx="0.9" fill="#0F8A6A"/>
      </svg>
      <span class="nav-brand">ResumAIner</span>
    </div>

    <!-- Desktop Navigation -->
    <nav class="nav-links-desktop">
      <router-link to="/home" active-class="router-link-exact-active">{{ $t('nav.home') }}</router-link>
      <router-link to="/profile/contact" active-class="router-link-exact-active">{{ $t('nav.myProfile') }}</router-link>
      <router-link to="/generate/vacancy" active-class="router-link-exact-active">{{ $t('nav.generateResume') }}</router-link>
      <router-link v-if="isAdmin" to="/admin" active-class="router-link-exact-active">{{ $t('nav.admin') }}</router-link>
    </nav>

    <!-- Right side -->
    <div class="nav-right">
      <!-- Hamburger first on mobile -->
      <button class="hamburger-btn" @click="mobileMenuOpen = true" :aria-label="$t('nav.mobileMenu')">
        <i class="pi pi-bars"></i>
      </button>
      <LanguageSwitcher />
      <button class="logout-btn" v-tooltip.top="$t('nav.logout')" :aria-label="$t('nav.logout')" @click="handleLogout" :disabled="loggingOut">
        <i class="pi pi-sign-out"></i>
      </button>
    </div>

    <!-- Mobile menu overlay -->
    <div v-if="mobileMenuOpen" class="mobile-menu-overlay" @click.self="mobileMenuOpen = false">
      <div class="mobile-menu-panel">
        <router-link to="/home" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-home"></i> {{ $t('nav.home') }}
        </router-link>
        <router-link to="/profile/contact" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-user"></i> {{ $t('nav.myProfile') }}
        </router-link>
        <router-link to="/generate/vacancy" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-file-plus"></i> {{ $t('nav.generateResume') }}
        </router-link>
        <router-link v-if="isAdmin" to="/admin" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-shield"></i> {{ $t('nav.admin') }}
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const router = useRouter()
const { role, logout } = useAuth()
const loggingOut = ref(false)
const mobileMenuOpen = ref(false)

const isAdmin = computed(() => role.value === 'ADMIN')

async function handleLogout() {
  loggingOut.value = true
  try {
    await logout()
    router.push('/auth')
  } finally {
    loggingOut.value = false
  }
}
</script>

<style scoped>
.resumainer-nav {
  background: #fff;
  border-bottom: 1px solid #E2E6EE;
  padding: 0 32px;
  height: 68px;
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 36px;
}
.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  height: 48px;
  flex-shrink: 0;
}
.nav-brand {
  font-family: 'Manrope', system-ui, sans-serif;
  font-size: 1.15rem;
  font-weight: 700;
  color: #10233F;
  letter-spacing: -0.3px;
}
.nav-links-desktop {
  display: flex;
  gap: 4px;
  flex: 1;
}
.nav-links-desktop a {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #5D718B;
  text-decoration: none;
  transition: all 0.15s;
  white-space: nowrap;
}
.nav-links-desktop a:hover,
.nav-links-desktop a.router-link-exact-active {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
.hamburger-btn {
  display: none;
  background: none;
  border: none;
  color: #5D718B;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  font-size: 1.25rem;
  transition: all 0.15s;
}
.hamburger-btn:hover {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.logout-btn {
  background: none;
  border: none;
  color: #5D718B;
  cursor: pointer;
  padding: 8px 10px;
  border-radius: 8px;
  transition: all 0.15s;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
}
.logout-btn:hover {
  background: rgba(15,157,122,0.08);
  color: #C2410C;
}
.mobile-menu-overlay {
  display: block;
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0,0,0,0.3);
}
.mobile-menu-panel {
  background: #fff;
  width: 260px;
  height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  box-shadow: 4px 0 20px rgba(0,0,0,0.1);
  animation: slideRight 0.2s ease-out;
}
@keyframes slideRight {
  from { transform: translateX(-20px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
.mobile-menu-panel a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #10233F;
  text-decoration: none;
  transition: all 0.12s;
}
.mobile-menu-panel a:hover,
.mobile-menu-panel a.router-link-exact-active {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.mobile-menu-panel a .pi {
  font-size: 1.1rem;
  color: #8091A7;
}
@media (max-width: 640px) {
  .resumainer-nav {
    padding: 0 16px;
    gap: 8px;
    height: 60px;
  }
  .nav-links-desktop {
    display: none;
  }
  .hamburger-btn {
    display: flex;
    align-items: center;
  }
  .nav-right {
    gap: 6px;
  }
  .logout-btn {
    padding: 6px 8px;
  }
}
</style>
