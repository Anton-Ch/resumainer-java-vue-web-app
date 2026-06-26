<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <div class="admin-header">
        <router-link to="/admin/users" class="back-link">
          <i class="pi pi-arrow-left"></i> {{ $t('admin.userDetails.back') }}
        </router-link>
        <h1>{{ $t('admin.userDetails.title') }}</h1>
      </div>

      <!-- Placeholder: user details tabs will be implemented in Phase 10 -->
      <div class="vue-card placeholder-card">
        <i class="pi pi-user" style="font-size: 2rem; color: var(--vue-accent-primary);"></i>
        <p class="placeholder-text">{{ $t('admin.userDetails.placeholder') }}</p>
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
  margin: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--vue-accent-primary);
  text-decoration: none;
  font-size: var(--vue-text-sm);
  margin-bottom: var(--vue-space-2);
}

.back-link:hover {
  text-decoration: underline;
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
