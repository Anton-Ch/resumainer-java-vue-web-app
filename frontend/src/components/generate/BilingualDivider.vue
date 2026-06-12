<template>
  <div class="bilingual-divider">
    <div class="divider-line"></div>
    <span class="divider-label">{{ label }}</span>
    <div class="divider-line"></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ label: string }>()
</script>

<style scoped>
.bilingual-divider { display: flex; align-items: center; gap: 1rem; margin: 1rem 0; }
.divider-line { flex: 1; height: 1px; background: #e0e0e0; }
.divider-label { font-weight: 600; color: #666; font-size: 0.9rem; white-space: nowrap; }
</style>
