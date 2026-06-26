<template>
  <div class="nav-cards">
    <button class="vue-card nav-card" @click="goTo('users')">
      <div class="nav-card-icon nav-icon-blue">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2" fill="none"/>
          <path d="M4 20c0-4 3.6-8 8-8s8 4 8 8" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
        </svg>
      </div>
      <span class="nav-card-title">{{ $t('admin.quickActions.users') }}</span>
      <span class="nav-card-arrow">&rarr;</span>
    </button>

    <button class="vue-card nav-card" @click="goTo('resumes')">
      <div class="nav-card-icon nav-icon-emerald">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <rect x="4" y="2" width="16" height="20" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
          <line x1="8" y1="8" x2="16" y2="8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <line x1="8" y1="13" x2="16" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <line x1="8" y1="18" x2="13" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <span class="nav-card-title">{{ $t('admin.quickActions.resumes') }}</span>
      <span class="nav-card-arrow">&rarr;</span>
    </button>

    <button class="vue-card nav-card" @click="goTo('ai-models')">
      <div class="nav-card-icon nav-icon-violet">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <rect x="2" y="2" width="20" height="20" rx="4" stroke="currentColor" stroke-width="2" fill="none"/>
          <circle cx="9" cy="9" r="2" stroke="currentColor" stroke-width="2" fill="none"/>
          <circle cx="15" cy="15" r="2" stroke="currentColor" stroke-width="2" fill="none"/>
          <line x1="10.5" y1="10.5" x2="13.5" y2="13.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <span class="nav-card-title">{{ $t('admin.quickActions.aiModels') }}</span>
      <span class="nav-card-arrow">&rarr;</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const emit = defineEmits<{
  scrollToResumes: []
}>()

function goTo(section: string) {
  if (section === 'users') {
    router.push('/admin/users')
  } else if (section === 'resumes') {
    emit('scrollToResumes')
  } else if (section === 'ai-models') {
    router.push('/admin/ai-models')
  }
}
</script>

<style scoped>
.nav-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--vue-space-6);
}
.nav-card {
  display: flex;
  align-items: center;
  gap: var(--vue-space-4);
  cursor: pointer;
  border: 1px solid var(--vue-border-soft);
  transition: box-shadow var(--vue-motion-base) var(--vue-ease-standard),
              border-color var(--vue-motion-fast) var(--vue-ease-standard);
  text-align: left;
  font-family: inherit;
  font-size: inherit;
}
.nav-card:hover {
  box-shadow: var(--vue-shadow-elevated);
  border-color: var(--vue-border-default);
}
.nav-card-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--vue-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.nav-card-icon svg {
  width: 20px;
  height: 20px;
}
.nav-icon-blue {
  background: var(--vue-accent-bg-blue);
  color: var(--vue-accent-blue);
}
.nav-icon-emerald {
  background: var(--vue-accent-bg-primary);
  color: var(--vue-accent-primary);
}
.nav-icon-violet {
  background: var(--vue-accent-bg-violet);
  color: var(--vue-accent-violet);
}
.nav-card-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  flex: 1;
}
.nav-card-arrow {
  color: var(--vue-text-muted);
  font-size: 18px;
  transition: transform var(--vue-motion-fast) var(--vue-ease-standard);
}
.nav-card:hover .nav-card-arrow {
  transform: translateX(3px);
}
@media (max-width: 639px) {
  .nav-cards {
    grid-template-columns: 1fr;
  }
}
@media (min-width: 640px) and (max-width: 1023px) {
  .nav-cards {
    grid-template-columns: 1fr;
  }
}
</style>
