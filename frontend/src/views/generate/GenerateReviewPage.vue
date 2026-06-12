<template>
  <div class="generate-layout">
    <GenerateStepper :current-step="3" :steps="steps" />
    <div class="generate-card">
      <h2 class="card-title">{{ $t('generate.review.title') }}</h2>

      <div v-if="loading" class="loading-state">
        <WhimsicalLoader />
        <p>{{ $t('generate.review.loading') }}</p>
      </div>

      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
      </div>

      <ReviewStepForm v-else :review-data="reviewData" @finalize="onFinalize" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import * as generateApi from '@/services/generateResumeService'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import ReviewStepForm from '@/components/generate/ReviewStepForm.vue'
import WhimsicalLoader from '@/components/generate/WhimsicalLoader.vue'

const route = useRoute()
const router = useRouter()
const { finalizeResume, state } = useGenerateResumeFlow()

const steps = [
  { label: 'Vacancy', route: '/generate/vacancy' },
  { label: 'Settings', route: '/generate/settings' },
  { label: 'Review', route: '/generate/review' },
  { label: 'Export', route: '/generate/export' }
]

const reviewData = ref<any>(null)
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  if (!state.value.requestId) {
    router.push('/generate/vacancy')
    return
  }
  try {
    reviewData.value = await generateApi.getReview(state.value.requestId)
  } catch (err: any) {
    error.value = err.message || 'Failed to load review data.'
  } finally {
    loading.value = false
  }
})

async function onFinalize(level: string) {
  await finalizeResume(level as any)
}
</script>
