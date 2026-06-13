<template>
  <div class="settings-form">
    <div class="vue-card">
      <div class="settings-section">
        <h3 class="vue-h4" style="margin-bottom:16px;">{{ $t('generate.settings.languageMode') }}</h3>
        <div class="radio-group" role="radiogroup" :aria-label="$t('generate.settings.languageMode')">
          <div v-for="opt in languageOptions" :key="opt.value" class="radio-option" @click="form.languageMode = opt.value">
            <RadioButton :inputId="'language-'+opt.value" :value="opt.value" v-model="form.languageMode" />
            <label :for="'language-'+opt.value">{{ opt.label }}</label>
          </div>
        </div>
      </div>

      <hr class="vue-divider" style="margin:24px 0;" />

      <div class="settings-section">
        <h3 class="vue-h4" style="margin-bottom:16px;">{{ $t('generate.settings.adaptationLevel') }}</h3>
        <div class="radio-group" role="radiogroup" :aria-label="$t('generate.settings.adaptationLevel')">
          <div v-for="opt in adaptationOptions" :key="opt.value" class="radio-option" @click="form.adaptationSelection = opt.value">
            <RadioButton :inputId="'adaptation-'+opt.value" :value="opt.value" v-model="form.adaptationSelection" />
            <label :for="'adaptation-'+opt.value">{{ opt.label }}</label>
          </div>
        </div>
      </div>

      <hr class="vue-divider" style="margin:24px 0;" />

      <div class="settings-section">
        <h3 class="vue-h4" style="margin-bottom:16px;">{{ $t('generate.settings.aiModel') }}</h3>
        <Select v-model="form.aiModelId" :options="modelOptions" optionLabel="displayName" optionValue="id" class="field-input" :placeholder="$t('generate.settings.aiModelPlaceholder')">
          <template #empty>{{ $t('generate.settings.noModels') }}</template>
        </Select>
      </div>

      <hr class="vue-divider" style="margin:24px 0;" />

      <div class="settings-section">
        <div class="checkbox-row">
          <Checkbox v-model="form.includeCoverLetter" :binary="true" inputId="includeCoverLetter" />
          <label for="includeCoverLetter">{{ $t('generate.settings.includeCoverLetter') }}</label>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import * as generateApi from '@/services/generateResumeService'
import RadioButton from 'primevue/radiobutton'
import Select from 'primevue/select'
import Checkbox from 'primevue/checkbox'

const form = reactive({ languageMode: 'ENGLISH_ONLY', adaptationSelection: 'BALANCED', aiModelId: '', includeCoverLetter: false })
defineExpose({ form })

const languageOptions = [
  { label: 'English only', value: 'ENGLISH_ONLY' },
  { label: 'Russian only', value: 'RUSSIAN_ONLY' },
  { label: 'Bilingual', value: 'BILINGUAL' }
]

const adaptationOptions = [
  { label: 'Minimal', value: 'MINIMAL' },
  { label: 'Balanced', value: 'BALANCED' },
  { label: 'Maximum', value: 'MAXIMUM' },
  { label: 'All levels', value: 'ALL' }
]

const modelOptions = ref<generateApi.AiModelDto[]>([])

onMounted(async () => {
  try {
    modelOptions.value = await generateApi.getAiModels()
    if (modelOptions.value.length > 0) form.aiModelId = modelOptions.value[0].id
  } catch { /* models not available */ }
})
</script>

<style scoped>
.settings-form { display: flex; flex-direction: column; gap: 24px; }
.settings-section { }
.radio-group { display: flex; flex-direction: column; gap: 8px; }
.radio-option { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 0; }
.checkbox-row { display: flex; align-items: center; gap: 8px; }
.field-input { width: 100%; }
</style>
