<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <div class="admin-header">
        <h1>{{ $t('admin.users.title') }}</h1>
        <p class="admin-subtitle">{{ $t('admin.users.subtitle') }}</p>
      </div>

      <!-- Placeholder: users table will be implemented in Phase 9 -->
      <div class="vue-card placeholder-card">
        <i class="pi pi-users" style="font-size: 2rem; color: var(--vue-accent-primary);"></i>
        <p class="placeholder-text">{{ $t('admin.users.placeholder') }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.page-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: var(--vue-space-6);
}

.admin-header {
  margin-bottom: var(--vue-space-6);
}

.admin-header h1 {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-2xl);
  font-weight: 700;
  color: var(--vue-text-primary);
  margin: 0 0 0.25rem;
}

.admin-subtitle {
  color: var(--vue-text-secondary);
  font-size: var(--vue-text-sm);
  margin: 0;
}

.placeholder-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--vue-space-4);
  padding: 3rem 2rem;
  text-align: center;
}

.placeholder-text {
  color: var(--vue-text-secondary);
  font-size: var(--vue-text-md);
  margin: 0;
  max-width: 400px;
}
</style>
