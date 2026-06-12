<template>
  <div class="generate-layout">
    <GenerateStepper :current-step="1" :steps="steps" />
    <div class="generate-card">
      <h2 class="card-title">{{ $t('generate.vacancy.title') }}</h2>
      <p class="card-desc">{{ $t('generate.vacancy.description') }}</p>
      <VacancyStepForm @continue="onContinue" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import VacancyStepForm from '@/components/generate/VacancyStepForm.vue'

const router = useRouter()
const { submitVacancy } = useGenerateResumeFlow()

const steps = [
  { label: 'Vacancy', route: '/generate/vacancy' },
  { label: 'Settings', route: '/generate/settings' },
  { label: 'Review', route: '/generate/review' },
  { label: 'Export', route: '/generate/export' }
]

async function onContinue(data: any) {
  await submitVacancy(data)
}
</script>
