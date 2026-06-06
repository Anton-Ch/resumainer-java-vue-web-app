<template>
  <div class="page">
    <AppHeader />
    <main class="page-main">
      <div class="placeholder-content">
        <i class="pi pi-building" style="font-size: 3rem; color: var(--muted-light, #8091A7); margin-bottom: 1rem;"></i>
        <h1>{{ $t('placeholder.title') }}</h1>
        <p class="placeholder-text">{{ $t('placeholder.text') }}</p>
        <router-link to="/home">
          <Button :label="$t('placeholder.back')" icon="pi pi-arrow-left" class="p-button-text" />
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import Button from 'primevue/button'
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
.placeholder-content {
  text-align: center;
  max-width: 480px;
}
.placeholder-text {
  color: var(--text-secondary, #5D718B);
  margin: 0.5rem 0 1.5rem;
  font-size: 1rem;
}
</style>
