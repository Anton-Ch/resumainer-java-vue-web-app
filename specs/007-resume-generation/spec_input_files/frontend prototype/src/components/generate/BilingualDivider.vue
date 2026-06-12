<template>
  <div class="bilingual-divider">
    <hr class="vue-divider" />
    <span class="divider-label">{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label?: string
}>()
</script>

<style scoped>
.bilingual-divider {
  position: relative;
  margin: 24px 0;
}
.divider-label {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  padding: 0 16px;
  font-size: 0.82rem;
  font-weight: 500;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
</style>
