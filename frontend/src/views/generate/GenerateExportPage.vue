<template>
  <div class="generate-layout">
    <GenerateStepper :current-step="4" :steps="steps" />
    <div class="generate-card">
      <h2 class="card-title">{{ $t('generate.export.title') }}</h2>
      <p class="card-desc">{{ $t('generate.export.description') }}</p>

      <div v-if="loading" class="loading-state">
        <WhimsicalLoader />
        <p>{{ $t('generate.export.loading') }}</p>
      </div>

      <ExportResult v-else-if="exportData" :export-data="exportData" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import * as generateApi from '@/services/generateResumeService'
import type { ExportResultDto } from '@/services/generateResumeService'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import ExportResult from '@/components/generate/ExportResult.vue'
import WhimsicalLoader from '@/components/generate/WhimsicalLoader.vue'

const router = useRouter()
const { t } = useI18n()
const { state } = useGenerateResumeFlow()

const steps = computed(() => [
  { label: t('generate.steps.vacancy'), route: '/generate/vacancy' },
  { label: t('generate.steps.settings'), route: '/generate/settings' },
  { label: t('generate.steps.review'), route: '/generate/review' },
  { label: t('generate.steps.export'), route: '/generate/export' }
])

const exportData = ref<ExportResultDto | null>(null)
const loading = ref(true)

onMounted(async () => {
  if (!state.value.requestId) {
    router.push('/generate/vacancy')
    return
  }
  try {
    exportData.value = await generateApi.getExport(state.value.requestId)
  } catch {
    // Export data may not be available yet
  } finally {
    loading.value = false
  }
})
</script>
