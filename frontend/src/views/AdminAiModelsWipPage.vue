<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <div class="wip-content">
        <i class="pi pi-spin pi-cog" style="font-size: 2.5rem; color: var(--vue-accent-violet);"></i>
        <h1>{{ $t('admin.aiModels.title') }}</h1>
        <p class="wip-text">{{ $t('admin.aiModels.wip') }}</p>
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
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.wip-content {
  text-align: center;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--vue-space-4);
}

.wip-content h1 {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-2xl);
  font-weight: 700;
  color: var(--vue-text-primary);
  margin: 0;
}

.wip-text {
  color: var(--vue-text-secondary);
  font-size: var(--vue-text-md);
  margin: 0;
}
</style>
