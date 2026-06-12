<template>
  <div class="record-group">
    <div v-for="(variant, vIdx) in variants" :key="vIdx" class="record-variant">
      <div class="variant-chip-row">
        <span v-if="showLevel" class="vue-chip" :class="levelChip(variant.adaptationLevel)">
          {{ variant.adaptationLevel }}
        </span>
        <span v-if="showLanguage" class="vue-chip" style="background: #EEF4FF; border-color: #2F6BFF; color: #2F6BFF;">
          {{ variant.language === 'EN' ? 'English' : 'Russian' }}
        </span>
      </div>
      <component :is="fieldComponent" v-bind="variant" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GeneratedVariant, AdaptationLevel } from '@/types/generate'

defineProps<{
  variants: GeneratedVariant[]
  showLevel: boolean
  showLanguage: boolean
  fieldComponent: any
}>()

function levelChip(level: AdaptationLevel): string {
  if (level === 'Minimal') return 'vue-chip-primary'
  if (level === 'Maximum') return 'vue-chip-primary'
  return ''
}
</script>

<style scoped>
.record-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.record-variant {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.variant-chip-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
