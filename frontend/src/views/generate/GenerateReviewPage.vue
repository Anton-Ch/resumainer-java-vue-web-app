<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.review.title') }}</h1>

      <GenerateStepper :currentStep="2" :disabledSteps="[3]" />

      <div class="step-content" v-if="reviewModel">
        <ReviewStepForm
          :en-variants="reviewModel.enVariants"
          :ru-variants="reviewModel.ruVariants"
          :is-bilingual="reviewModel.isBilingual"
          :show-levels="reviewModel.showLevels"
          v-model:active-tab="activeTab"
          v-model:selected-level="selectedLevel"
          @save="handleSaveAndFinalize"
        />
      </div>

      <div v-else-if="loadError" class="vue-card" style="text-align:center;padding:48px;">
        <i class="pi pi-exclamation-triangle" style="font-size:2rem;color:#8091A7;margin-bottom:12px;"></i>
        <p>{{ $t('generate.review.noData') }}</p>
        <p style="font-size:0.85rem;color:#999;margin-top:8px;">{{ loadError }}</p>
        <Button :label="$t('generate.review.goToSettings')" class="p-button-outlined" @click="$router.push('/generate/settings')" style="margin-top:12px;" />
      </div>

      <div v-else style="text-align:center;padding:48px;">
        <i class="pi pi-spin pi-spinner" style="font-size:2rem;color:#8091A7;"></i>
        <p style="margin-top:12px;color:#8091A7;">{{ $t('generate.review.loading') }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import * as generateApi from '@/services/generateResumeService'
import type { GenerationReviewDto, ReviewViewModel, PrototypeLevel } from '@/types/generate'
import { adaptGenerationReviewDto, buildReviewUpdatePayloadSimple, toBackendLevel } from '@/utils/generateReviewAdapter'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import ReviewStepForm from '@/components/generate/ReviewStepForm.vue'
import Button from 'primevue/button'

const router = useRouter()
const { state, finalizeResume } = useGenerateResumeFlow()

const reviewModel = ref<ReviewViewModel | null>(null)
const loadError = ref<string | null>(null)
const activeTab = ref('positioning')
const selectedLevel = ref<PrototypeLevel>('Balanced')

onMounted(async () => {
  if (!state.value.requestId || state.value.requestId === 'null') {
    router.push('/generate/vacancy')
    return
  }
  try {
    const rawReview: GenerationReviewDto = await generateApi.getReview(state.value.requestId)
    reviewModel.value = adaptGenerationReviewDto(rawReview)

    // Initialize selectedLevel from actual generated data
    // Collect unique adaptation levels present in the review
    const allVariants = [...reviewModel.value.enVariants, ...reviewModel.value.ruVariants]
    const uniqueLevels = new Set<PrototypeLevel>()
    for (const v of allVariants) {
      uniqueLevels.add(v.adaptationLevel)
    }
    // Auto-select the first available level (prefer Balanced, then first found)
    if (uniqueLevels.has('Balanced')) {
      selectedLevel.value = 'Balanced'
    } else if (uniqueLevels.size > 0) {
      selectedLevel.value = uniqueLevels.values().next().value!
    }
  } catch (err: any) {
    loadError.value = err.message || 'Failed to load review data.'
  }
})

async function handleSaveAndFinalize() {
  if (!state.value.requestId || !reviewModel.value) return

  // Build save payload for edited fields (includes personal_info and skills)
  const payload = buildReviewUpdatePayloadSimple(reviewModel.value)

  // Save changes if any
  if (Object.keys(payload.fieldUpdates).length > 0) {
    await generateApi.saveReview(state.value.requestId, payload)
  }

  // Finalize with selected adaptation level
  await finalizeResume(toBackendLevel(selectedLevel.value) as any)

  router.push('/generate/export')
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { flex: 1; max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
.step-content { margin-top: 32px; }
@media (max-width: 640px) { .main-content { padding: 20px 16px; } }
</style>
