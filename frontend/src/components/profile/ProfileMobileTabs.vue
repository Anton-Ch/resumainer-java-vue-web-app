<template>
  <nav class="profile-mobile-tabs">
    <router-link
      v-for="section in sections"
      :key="section.key"
      :to="section.route"
      class="mobile-tab-item"
      :class="{ active: activeSection === section.key }"
    >
      <div class="mobile-tab-name">{{ section.label }}</div>
      <div class="mobile-tab-status" :class="'status-' + section.statusType">
        {{ section.statusText }}
      </div>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import type { SidebarSection } from '@/types/profile'

defineProps<{
  sections: SidebarSection[]
  activeSection: string
}>()
</script>

<style scoped>
.profile-mobile-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 20px;
}
.mobile-tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 10px 6px;
  border-radius: var(--vue-radius-md);
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  text-decoration: none;
  transition: all var(--vue-motion-fast) var(--vue-ease-standard);
  gap: 2px;
}
.mobile-tab-item:hover {
  background: var(--vue-bg-subtle);
}
.mobile-tab-item.active {
  background: var(--vue-accent-bg-primary);
  border-color: var(--vue-accent-primary);
}
.mobile-tab-name {
  font-family: var(--vue-font-body);
  font-size: 12px;
  font-weight: 600;
  color: var(--vue-text-primary);
  line-height: 1.2;
}
.mobile-tab-item.active .mobile-tab-name {
  color: var(--vue-accent-primary);
}
.mobile-tab-status {
  font-family: var(--vue-font-body);
  font-size: 10px;
  color: var(--vue-text-muted);
  line-height: 1.1;
}
.mobile-tab-status.status-completed {
  color: var(--vue-accent-primary);
}
.mobile-tab-status.status-incomplete {
  color: var(--vue-accent-warning);
}
</style>
