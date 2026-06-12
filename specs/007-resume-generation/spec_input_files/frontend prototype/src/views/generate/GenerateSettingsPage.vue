<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.title') }}</h1>

      <GenerateStepper
        :currentStep="1"
        :disabledSteps="[2, 3]"
      />

      <div class="step-content">
        <SettingsStepForm
          :form="formState"
        />

        <div class="step-actions">
          <Button
            :label="$t('generate.settings.generate')"
            icon="pi pi-play"
            class="p-button-success p-button-lg"
            @click="handleGenerate"
          />
          <Button
            :label="$t('generate.vacancy.backToWorkspace')"
            class="p-button-outlined"
            @click="goHome"
          />
        </div>
      </div>
    </main>

    <!-- PROTOTYPE MOCK ONLY — THIS LOADER SIMULATES AI GENERATION FOR UX REVIEW ONLY. -->
    <WhimsicalLoader
      v-if="flow.isGenerating"
      :title="$t('generate.loader.generationTitle')"
      :hint="$t('generate.loader.text')"
    />
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import SettingsStepForm from '@/components/generate/SettingsStepForm.vue'
import WhimsicalLoader from '@/components/generate/WhimsicalLoader.vue'
import Button from 'primevue/button'

const router = useRouter()
const { formState, flow, startGeneration } = useGenerateResumeFlow()

function goHome() {
  router.push('/')
}

async function handleGenerate() {
  // PROTOTYPE MOCK ONLY — REPLACE WITH REAL GENERATION API CALL IN PRODUCTION.
  await startGeneration()
  // Generation done — navigate to review
  router.push('/generate/review')
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.main-content {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 36px 32px;
}
.step-content {
  margin-top: 32px;
}
.step-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}
@media (max-width: 640px) {
  .main-content {
    padding: 20px 16px;
  }
}
</style>
