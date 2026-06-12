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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import SettingsStepForm from '@/components/generate/SettingsStepForm.vue'

const router = useRouter()
const { t } = useI18n()
const { submitSettings } = useGenerateResumeFlow()

const steps = computed(() => [
  { label: t('generate.steps.vacancy'), route: '/generate/vacancy' },
  { label: t('generate.steps.settings'), route: '/generate/settings' },
  { label: t('generate.steps.review'), route: '/generate/review' },
  { label: t('generate.steps.export'), route: '/generate/export' }
])

async function onContinue(data: any) {
  await submitSettings(data)
}
</script>
