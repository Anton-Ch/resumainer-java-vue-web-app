<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.title') }}</h1>

      <GenerateStepper :currentStep="1" :disabledSteps="[2, 3]" />

      <div class="step-content">
        <SettingsStepForm ref="settingsFormRef" />

        <div class="step-actions">
          <Button
            :label="$t('generate.settings.generate')"
            icon="pi pi-play"
            class="p-button-success p-button-lg"
            :disabled="state.isLoading"
            :loading="state.isLoading"
            @click="handleGenerate"
          />
          <Button :label="$t('generate.vacancy.backToWorkspace')" class="p-button-outlined" @click="goHome" />
        </div>
      </div>
    </main>

    <WhimsicalLoader v-if="state.isLoading" :title="$t('generate.loader.generationTitle')" :hint="$t('generate.loader.text')" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import SettingsStepForm from '@/components/generate/SettingsStepForm.vue'
import WhimsicalLoader from '@/components/generate/WhimsicalLoader.vue'
import Button from 'primevue/button'

const router = useRouter()
const { state, submitSettings } = useGenerateResumeFlow()
const settingsFormRef = ref<InstanceType<typeof SettingsStepForm> | null>(null)

function goHome() { router.push('/') }

async function handleGenerate() {
  if (state.value.isLoading) return
  const data = settingsFormRef.value?.form
  if (!data) return
  await submitSettings({
    languageMode: data.languageMode as any,
    adaptationSelection: data.adaptationSelection as any,
    aiModelId: data.aiModelId || '',
    includeCoverLetter: data.includeCoverLetter
  })
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { flex: 1; max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
.step-content { margin-top: 32px; }
.step-actions { display: flex; align-items: center; gap: 12px; margin-top: 24px; flex-wrap: wrap; }
@media (max-width: 640px) { .main-content { padding: 20px 16px; } }
</style>
