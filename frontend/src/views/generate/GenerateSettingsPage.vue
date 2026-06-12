<template>
  <div class="generate-layout">
    <GenerateStepper :current-step="2" :steps="steps" />
    <div class="generate-card">
      <h2 class="card-title">{{ $t('generate.settings.title') }}</h2>
      <p class="card-desc">{{ $t('generate.settings.description') }}</p>
      <SettingsStepForm @continue="onContinue" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import SettingsStepForm from '@/components/generate/SettingsStepForm.vue'

const router = useRouter()
const { submitSettings } = useGenerateResumeFlow()

const steps = [
  { label: 'Vacancy', route: '/generate/vacancy' },
  { label: 'Settings', route: '/generate/settings' },
  { label: 'Review', route: '/generate/review' },
  { label: 'Export', route: '/generate/export' }
]

async function onContinue(data: any) {
  await submitSettings(data)
}
</script>
