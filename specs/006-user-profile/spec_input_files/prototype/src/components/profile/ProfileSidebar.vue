<template>
  <nav class="profile-sidebar">
    <div class="sidebar-title">{{ $t('profile.title') }}</div>
    <div class="sidebar-sections">
      <router-link
        v-for="section in sections"
        :key="section.key"
        :to="section.route"
        class="sidebar-item"
        :class="{ active: activeSection === section.key }"
      >
        <div class="sidebar-item-name">{{ section.label }}</div>
        <div class="sidebar-item-status" :class="'status-' + section.statusType">
          {{ section.statusText }}
        </div>
      </router-link>
    </div>
  </nav>
</template>

<script setup lang="ts">
import type { SectionStatus } from '@/types/profile'

defineProps<{
  sections: SectionStatus[]
  activeSection: string
}>()
</script>

<style scoped>
.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
}
.sidebar-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 700;
  color: var(--vue-text-primary);
  margin-bottom: 16px;
  letter-spacing: -0.01em;
}
.sidebar-sections {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.sidebar-item {
  display: block;
  padding: 10px 14px;
  border-radius: var(--vue-radius-md);
  text-decoration: none;
  transition: all var(--vue-motion-fast) var(--vue-ease-standard);
  border-left: 3px solid transparent;
}
.sidebar-item:hover {
  background: var(--vue-bg-subtle);
}
.sidebar-item.active {
  background: var(--vue-accent-bg-primary);
  border-left-color: var(--vue-accent-primary);
}
.sidebar-item-name {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  font-weight: 500;
  color: var(--vue-text-primary);
  margin-bottom: 2px;
}
.sidebar-item.active .sidebar-item-name {
  color: var(--vue-accent-primary);
}
.sidebar-item-status {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-text-muted);
}
.sidebar-item-status.status-completed {
  color: var(--vue-accent-primary);
}
.sidebar-item-status.status-incomplete {
  color: var(--vue-accent-warning);
}
.sidebar-item-status.status-count {
  color: var(--vue-text-muted);
}
.sidebar-item-status.status-no-records {
  color: var(--vue-text-muted);
}
</style>
