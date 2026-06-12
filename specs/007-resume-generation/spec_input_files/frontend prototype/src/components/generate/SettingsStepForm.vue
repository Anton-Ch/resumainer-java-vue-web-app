<template>
  <div class="settings-form">
    <div class="vue-card">
      <div class="settings-section">
        <h3 class="vue-h4" style="margin-bottom: 16px;">{{ $t('generate.settings.languageMode') }}</h3>
        <div class="radio-group" role="radiogroup" :aria-label="$t('generate.settings.languageMode')">
          <div
            v-for="opt in languageOptions"
            :key="opt.value"
            class="radio-option"
            @click="form.languageMode = opt.value as any"
          >
            <RadioButton
              :inputId="opt.value"
              :value="opt.value"
              v-model="form.languageMode"
            />
            <label :for="opt.value">{{ opt.label }}</label>
          </div>
        </div>
      </div>

      <hr class="vue-divider" style="margin: 24px 0;" />

      <div class="settings-section">
        <h3 class="vue-h4" style="margin-bottom: 16px;">{{ $t('generate.settings.adaptationLevel') }}</h3>
        <div class="radio-group" role="radiogroup" :aria-label="$t('generate.settings.adaptationLevel')">
          <div
            v-for="opt in adaptationOptions"
            :key="opt.value"
            class="radio-option"
            @click="form.adaptationSelection = opt.value as any"
          >
            <RadioButton
              :inputId="opt.value"
              :value="opt.value"
              v-model="form.adaptationSelection"
            />
            <label :for="opt.value">{{ opt.label }}</label>
          </div>
        </div>
        <p class="vue-body-sm" style="margin-top: 12px; color: #5D718B;">
          <span v-if="form.adaptationSelection === 'Minimal'">{{ $t('generate.settings.descriptions.minimal') }}</span>
          <span v-else-if="form.adaptationSelection === 'Balanced'">{{ $t('generate.settings.descriptions.balanced') }}</span>
          <span v-else-if="form.adaptationSelection === 'Maximum'">{{ $t('generate.settings.descriptions.maximum') }}</span>
          <span v-else>{{ $t('generate.settings.descriptions.allLevels') }}</span>
        </p>
      </div>

      <hr class="vue-divider" style="margin: 24px 0;" />

      <div class="settings-section">
        <div class="checkbox-row">
          <Checkbox
            inputId="includeCoverLetter"
            v-model="form.includeCoverLetter"
            :binary="true"
          />
          <label for="includeCoverLetter">{{ $t('generate.settings.includeCoverLetter') }}</label>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { GenerateResumeFormState, ResumeLanguageMode, AdaptationSelection } from '@/types/generate'
import RadioButton from 'primevue/radiobutton'
import Checkbox from 'primevue/checkbox'

const { t } = useI18n()

const props = defineProps<{
  form: GenerateResumeFormState
}>()

const emit = defineEmits<{
  (e: 'update:form', form: GenerateResumeFormState): void
}>()

const form = computed({
  get: () => props.form,
  set: (val) => emit('update:form', val)
})

const languageOptions = computed(() => [
  { label: t('generate.settings.englishOnly'), value: 'English only' as ResumeLanguageMode },
  { label: t('generate.settings.russianOnly'), value: 'Russian only' as ResumeLanguageMode },
  { label: t('generate.settings.bilingual'), value: 'Bilingual' as ResumeLanguageMode }
])

const adaptationOptions = computed(() => [
  { label: t('generate.settings.minimal'), value: 'Minimal' as AdaptationSelection },
  { label: t('generate.settings.balanced'), value: 'Balanced' as AdaptationSelection },
  { label: t('generate.settings.maximum'), value: 'Maximum' as AdaptationSelection },
  { label: t('generate.settings.allLevels'), value: 'All' as AdaptationSelection }
])
</script>

<style scoped>
.settings-section {
  display: flex;
  flex-direction: column;
}
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.radio-option {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0;
}
.radio-option label {
  cursor: pointer;
  font-size: 0.9rem;
  color: #10233F;
}
.checkbox-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.checkbox-row label {
  cursor: pointer;
  font-size: 0.9rem;
  color: #10233F;
}
</style>
