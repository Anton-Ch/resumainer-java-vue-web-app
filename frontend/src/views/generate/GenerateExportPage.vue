<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.export.title') }}</h1>

      <GenerateStepper :currentStep="3" :disabledSteps="[]" />

      <div class="step-content" v-if="exportData">
        <ExportResult :export-data="exportData" />

        <div class="export-footer">
          <Button :label="$t('generate.export.generateAnother')" icon="pi pi-plus" class="p-button-success" @click="handleGenerateAnother" />
        </div>
      </div>

      <div v-else class="vue-card" style="text-align:center;padding:48px;">
        <i class="pi pi-exclamation-triangle" style="font-size:2rem;color:#8091A7;margin-bottom:12px;"></i>
        <p>{{ $t('generate.export.noData') }}</p>
        <Button :label="$t('generate.export.goToReview')" class="p-button-outlined" @click="$router.push('/generate/review')" style="margin-top:12px;" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import * as generateApi from '@/services/generateResumeService'
import type { ExportResultDto } from '@/services/generateResumeService'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import ExportResult from '@/components/generate/ExportResult.vue'
import Button from 'primevue/button'

const router = useRouter()
const { state } = useGenerateResumeFlow()
const exportData = ref<ExportResultDto | null>(null)

onMounted(async () => {
  if (!state.value.requestId) { router.push('/generate/vacancy'); return }
  try { exportData.value = await generateApi.getExport(state.value.requestId) } catch { /* ignore */ }
})

function handleGenerateAnother() {
  router.push('/generate/vacancy')
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { flex: 1; max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
.step-content { margin-top: 32px; }
.export-footer { display: flex; justify-content: center; margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--vue-border-soft); }
@media (max-width: 640px) { .main-content { padding: 20px 16px; } }
</style>
