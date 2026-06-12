<template>
  <div class="radio-group">
    <div v-for="opt in options" :key="opt.value" class="radio-item">
      <RadioButton :inputId="opt.value" :value="opt.value" v-model="selected" @change="onChange" />
      <label :for="opt.value">{{ opt.label }}</label>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const selected = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const options = [
  { label: 'Minimal', value: 'MINIMAL' },
  { label: 'Balanced', value: 'BALANCED' },
  { label: 'Maximum', value: 'MAXIMUM' },
  { label: 'All levels', value: 'ALL' }
]

function onChange() { /* v-model handles it */ }
</script>
