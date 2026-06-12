<template>
  <div class="variant-textarea">
    <div class="variant-chip-row">
      <span v-if="adaptationLevel" class="vue-chip" :class="chipClass">{{ adaptationLevel }}</span>
      <span v-if="languageLabel" class="vue-chip" style="background: #EEF4FF; border-color: #2F6BFF; color: #2F6BFF;">{{ languageLabel }}</span>
    </div>
    <Textarea
      :modelValue="modelValue"
      @update:modelValue="$emit('update:modelValue', $event)"
      :rows="rows"
      class="variant-input"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Textarea from 'primevue/textarea'

const props = defineProps<{
  modelValue: string
  adaptationLevel?: string
  languageLabel?: string
  rows?: number
}>()

defineEmits<{
  (e: 'update:modelValue', val: string): void
}>()

const chipClass = computed(() => {
  if (!props.adaptationLevel) return ''
  const level = props.adaptationLevel.toLowerCase()
  if (level.includes('minimal') || level.includes('минималь')) return 'vue-chip-primary'
  if (level.includes('balanced') || level.includes('сбалан')) return 'vue-chip'
  if (level.includes('maximum') || level.includes('максима')) return 'vue-chip-primary'
  return ''
})
</script>

<style scoped>
.variant-textarea {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.variant-chip-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.variant-input {
  width: 100%;
}
</style>
