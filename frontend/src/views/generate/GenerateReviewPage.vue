<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.review.title') }}</h1>

      <GenerateStepper :currentStep="2" :disabledSteps="[3]" />

      <div class="step-content" v-if="reviewModel">
        <div v-if="finalizeError" class="vue-alert vue-alert-error" style="margin-bottom: 16px;">
          <i class="pi pi-exclamation-triangle" style="margin-top: 2px;"></i>
          <span>{{ finalizeError }}</span>
        </div>
        <ReviewStepForm
          :en-variants="reviewModel.enVariants"
          :ru-variants="reviewModel.ruVariants"
          :is-bilingual="reviewModel.isBilingual"
          :show-levels="reviewModel.showLevels"
          :is-finalizing="isFinalizing"
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
const isFinalizing = ref(false)
const finalizeError = ref<string | null>(null)

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
  if (isFinalizing.value) return  // block double-click

  isFinalizing.value = true
  finalizeError.value = null

  try {
    // Build save payload for edited fields (includes personal_info, skills, bullets)
    const payload = buildReviewUpdatePayloadSimple(reviewModel.value)

    // Save changes if any (wait for completion before finalizing)
    if (Object.keys(payload.fieldUpdates).length > 0) {
      await generateApi.saveReview(state.value.requestId, payload)
    }

    // Finalize with selected adaptation level
    // finalizeResume() handles navigation to /generate/export on success
    await finalizeResume(toBackendLevel(selectedLevel.value) as any)
  } catch (err: any) {
    const message = err?.message || 'Failed to finalize resume.'
    if (message.includes('409') || message.includes('Conflict') || message.includes('already in progress')) {
      finalizeError.value = 'Finalization is already in progress. Please wait.'
    } else if (message.includes('400') || message.includes('Bad Request')) {
      finalizeError.value = 'Invalid request. Please check your data and try again.'
    } else if (message.includes('422') || message.includes('Unprocessable')) {
      finalizeError.value = 'Some review fields contain invalid data. Please correct them and try again.'
    } else {
      finalizeError.value = message
    }
  } finally {
    isFinalizing.value = false
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { flex: 1; max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
.step-content { margin-top: 32px; }
@media (max-width: 640px) { .main-content { padding: 20px 16px; } }
</style>
