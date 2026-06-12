<template>
  <form @submit.prevent="onSubmit" class="settings-form">
    <div class="form-group">
      <label>{{ $t('generate.settings.languageMode') }}</label>
      <Select v-model="form.languageMode" :options="languageOptions" optionLabel="label" optionValue="value" />
    </div>
    <div class="form-group">
      <label>{{ $t('generate.settings.adaptationSelection') }}</label>
      <AdaptationLevelRadioGroup v-model="form.adaptationSelection" />
    </div>
    <div class="form-group">
      <label>{{ $t('generate.settings.aiModel') }}</label>
      <Select v-model="form.aiModelId" :options="modelOptions" optionLabel="displayName" optionValue="id">
        <template #empty>{{ $t('generate.settings.noModels') }}</template>
      </Select>
    </div>
    <div class="form-group">
      <div class="checkbox-row">
        <Checkbox v-model="form.includeCoverLetter" :binary="true" inputId="coverLetter" />
        <label for="coverLetter">{{ $t('generate.settings.includeCoverLetter') }}</label>
      </div>
    </div>
    <div class="form-actions">
      <Button type="submit" :label="$t('generate.settings.generate')" icon="pi pi-play" />
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import * as generateApi from '@/services/generateResumeService'
import AdaptationLevelRadioGroup from './AdaptationLevelRadioGroup.vue'

const emit = defineEmits<{ (e: 'continue', data: any): void }>()

const form = reactive({
  languageMode: 'ENGLISH_ONLY',
  adaptationSelection: 'BALANCED',
  aiModelId: '',
  includeCoverLetter: false
})

const languageOptions = [
  { label: 'English only', value: 'ENGLISH_ONLY' },
  { label: 'Russian only', value: 'RUSSIAN_ONLY' },
  { label: 'Bilingual', value: 'BILINGUAL' }
]

const modelOptions = ref<generateApi.AiModelDto[]>([])

onMounted(async () => {
  try {
    modelOptions.value = await generateApi.getAiModels()
    if (modelOptions.value.length > 0) {
      form.aiModelId = modelOptions.value[0].id
    }
  } catch {
    // Models not available yet
  }
})

function onSubmit() {
  emit('continue', { ...form })
}
</script>
