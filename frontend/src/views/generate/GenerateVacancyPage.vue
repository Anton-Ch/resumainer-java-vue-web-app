<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.title') }}</h1>

      <GenerateStepper :currentStep="0" :disabledSteps="[1, 2, 3]" />

      <div class="step-content">
        <VacancyStepForm ref="vacancyFormRef" />

        <div class="step-actions">
          <Button :label="$t('generate.vacancy.continue')" icon="pi pi-arrow-right" iconPos="right" class="p-button-success" @click="handleContinue" />
          <Button :label="$t('generate.vacancy.backToWorkspace')" class="p-button-outlined" @click="goHome" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import * as generateApi from '@/services/generateResumeService'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import VacancyStepForm from '@/components/generate/VacancyStepForm.vue'
import Button from 'primevue/button'

const router = useRouter()
const { submitVacancy, state } = useGenerateResumeFlow()
const vacancyFormRef = ref<InstanceType<typeof VacancyStepForm> | null>(null)
const aiModelId = ref('')

onMounted(async () => {
  try {
    const models = await generateApi.getAiModels()
    if (models.length > 0) {
      aiModelId.value = models[0].id
    }
  } catch { /* models not available */ }
})

async function handleContinue() {
  const data = vacancyFormRef.value?.form
  if (!data || !data.vacancyTitle?.trim() || !data.vacancyDescription?.trim()) return
  state.value.aiModelId = aiModelId.value
  await submitVacancy(data)
}

function goHome() { router.push('/') }
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { flex: 1; max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
.step-content { margin-top: 32px; }
.step-actions { display: flex; align-items: center; gap: 12px; margin-top: 24px; flex-wrap: wrap; }
@media (max-width: 640px) { .main-content { padding: 20px 16px; } }
</style>
