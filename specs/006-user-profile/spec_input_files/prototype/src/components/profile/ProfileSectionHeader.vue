<template>
  <div class="profile-section-header">
    <div class="section-header-top">
      <Button
        icon="pi pi-arrow-left"
        class="p-button-text p-button-sm back-btn"
        :label="$t('nav.home')"
        @click="$router.push('/home')"
      />
    </div>
    <h2 class="section-title section-title-area">{{ title }}</h2>
    <p v-if="purpose" class="section-purpose">{{ purpose }}</p>
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'

defineProps<{
  title: string
  purpose?: string
}>()
</script>

<style scoped>
.profile-section-header {
  margin-bottom: 24px;
}
.section-header-top {
  margin-bottom: 12px;
}
.back-btn {
  padding: 4px 8px !important;
  color: var(--vue-text-secondary) !important;
}
.section-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-2xl);
  font-weight: 700;
  color: var(--vue-text-primary);
  margin: 0 0 6px;
  letter-spacing: -0.015em;
}
.section-purpose {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  line-height: var(--vue-leading-body);
  margin: 0;
  max-width: 640px;
}
</style>
