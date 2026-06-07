<template>
  <div class="vue-alert vue-alert-warning empty-state-block">
    <i class="pi pi-info-circle"></i>
    <div>
      <strong>{{ title }}</strong>
      <p v-if="hint" class="empty-hint">{{ hint }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  hint?: string
}>()
</script>

<style scoped>
.empty-state-block {
  margin-bottom: 16px;
}
.empty-hint {
  margin: 4px 0 0 !important;
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
}
</style>
