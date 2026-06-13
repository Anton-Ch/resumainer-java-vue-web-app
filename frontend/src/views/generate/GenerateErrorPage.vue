<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.title') }}</h1>

      <GenerateStepper :currentStep="1" :disabledSteps="[2, 3]" />

      <div class="step-content">
        <div class="vue-card error-panel">
          <div class="error-icon"><i class="pi pi-exclamation-triangle"></i></div>
          <h2 class="vue-h3">{{ $t('generate.error.title') }}</h2>
          <p class="vue-body-sm">{{ $t('generate.error.description') }}</p>

          <div class="step-actions" style="justify-content:center;margin-top:24px;">
            <Button :label="$t('generate.error.tryAgain')" icon="pi pi-refresh" class="p-button-success" @click="onRetry" />
            <Button :label="$t('generate.error.changeSettings')" class="p-button-outlined" @click="onChangeSettings" />
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'

const { retryGeneration, goToSettings } = useGenerateResumeFlow()

async function onRetry() { await retryGeneration() }
function onChangeSettings() { goToSettings() }
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { flex: 1; max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
.step-content { margin-top: 32px; }
.error-panel { text-align: center; padding: 48px; }
.error-icon { width: 56px; height: 56px; margin: 0 auto 16px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #FFF3E0; color: #F57C00; font-size: 1.5rem; }
@media (max-width: 640px) { .main-content { padding: 20px 16px; } }
</style>
