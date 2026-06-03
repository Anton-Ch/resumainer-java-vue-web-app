<template>
  <header class="vue-topbar">
    <div class="topbar-left">
      <svg class="topbar-logo-icon" viewBox="0 0 32 32" fill="none" aria-hidden="true" shape-rendering="geometricPrecision">
        <rect x="5" y="3.5" width="22" height="25" rx="4" fill="#FFFDF8" stroke="#17211D" stroke-width="2.4"/>
        <circle cx="16" cy="12.2" r="5.8" fill="none" stroke="#17211D" stroke-width="1.9"/>
        <circle cx="16" cy="12.2" r="2.8" fill="#0F8A6A"/>
        <rect x="9.2" y="24" width="13.6" height="3" rx="0.9" fill="#0F8A6A"/>
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
