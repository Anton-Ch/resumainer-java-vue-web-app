<template>
  <header class="vue-topbar">
    <div class="topbar-left">
      <svg class="topbar-logo-icon" viewBox="0 0 28 28" fill="none" aria-hidden="true">
        <rect x="2" y="2" width="24" height="24" rx="6" fill="#0F9D7A"/>
        <path d="M8 10h12M8 14h12M8 18h8" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <span class="topbar-brand">ResumAIner</span>
      <span v-if="roleBadge" class="vue-chip" :class="roleBadgeClass">{{ roleBadge }}</span>
    </div>
    <div class="topbar-right">
      <LanguageSwitcher />
      <button
        v-if="showLogout"
        class="logout-btn"
        @click="handleLogout"
        :disabled="loggingOut"
      >
        {{ $t('nav.logout') }}
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const props = withDefaults(defineProps<{
  showLogout?: boolean
}>(), {
  showLogout: true
})

const router = useRouter()
const { role, logout } = useAuth()
const loggingOut = ref(false)

const roleBadge = computed(() => role.value || null)
const roleBadgeClass = computed(() => role.value === 'ADMIN' ? 'vue-chip-role' : '')

async function handleLogout() {
  loggingOut.value = true
  try {
    await logout()
    router.push('/login')
  } finally {
    loggingOut.value = false
  }
}
</script>

<style scoped>
.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topbar-logo-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.topbar-brand {
  font-family: var(--vue-font-heading);
  font-size: 18px;
  font-weight: 700;
  color: var(--vue-text-primary);
  letter-spacing: -0.3px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logout-btn {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  font-weight: 600;
  color: var(--vue-accent-blue);
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 8px;
  transition: background var(--vue-motion-fast) var(--vue-ease-standard);
}

.logout-btn:hover {
  background: var(--vue-accent-bg-blue);
}

.logout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
