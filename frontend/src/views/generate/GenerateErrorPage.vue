<template>
  <div class="generate-layout">
    <div class="generate-card error-card">
      <div class="error-icon">⚠</div>
      <h2 class="card-title">{{ $t('generate.error.title') }}</h2>
      <p class="card-desc">{{ $t('generate.error.description') }}</p>

      <div class="error-actions">
        <Button :label="$t('generate.error.tryAgain')" icon="pi pi-refresh" @click="onRetry" />
        <Button :label="$t('generate.error.changeSettings')" icon="pi pi-cog" class="p-button-outlined" @click="onChangeSettings" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'

const { retryGeneration, goToSettings } = useGenerateResumeFlow()

async function onRetry() {
  await retryGeneration()
}

function onChangeSettings() {
  goToSettings()
}
</script>

<style scoped>
.error-card { text-align: center; padding: 3rem; }
.error-icon { font-size: 3rem; margin-bottom: 1rem; }
.error-actions { display: flex; gap: 1rem; justify-content: center; margin-top: 2rem; }
</style>
