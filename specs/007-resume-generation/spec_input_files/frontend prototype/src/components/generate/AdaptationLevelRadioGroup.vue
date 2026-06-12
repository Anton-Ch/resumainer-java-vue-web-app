<template>
  <div class="adaptation-selection">
    <h3 class="vue-h4" style="margin-bottom: 12px;">{{ title }}</h3>

    <div class="radio-levels" role="radiogroup" :aria-label="title">
      <div
        v-for="opt in options"
        :key="opt.value"
        class="radio-level"
        @click="selectLevel(opt.value)"
      >
        <RadioButton
          :inputId="'level-' + opt.value"
          :value="opt.value"
          v-model="selectedLevel"
        />
        <label :for="'level-' + opt.value">{{ opt.label }}</label>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { AdaptationLevel } from '@/types/generate'
import RadioButton from 'primevue/radiobutton'

const { t } = useI18n()

const props = defineProps<{
  modelValue: AdaptationLevel
  title: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: AdaptationLevel): void
}>()

const selectedLevel = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const options = computed(() => [
  { label: t('generate.settings.minimal'), value: 'Minimal' as AdaptationLevel },
  { label: t('generate.settings.balanced'), value: 'Balanced' as AdaptationLevel },
  { label: t('generate.settings.maximum'), value: 'Maximum' as AdaptationLevel }
])

function selectLevel(val: AdaptationLevel) {
  emit('update:modelValue', val)
}
</script>

<style scoped>
.adaptation-selection {
  padding: 20px 0;
}
.radio-levels {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.radio-level {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0;
}
.radio-level label {
  cursor: pointer;
  font-size: 0.9rem;
  color: #10233F;
}
</style>
