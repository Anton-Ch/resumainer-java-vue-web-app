/**
 * Generate Resume flow composable.
 * Manages wizard state, API calls, and data flow between steps.
 */
import { ref, computed } from 'vue'
import type {
  GenerateFlowState,
  LanguageMode,
  AdaptationSelection,
  AdaptationLevel,
  WizardStep
} from '@/types/generate'
import * as generateApi from '@/services/generateResumeService'
import { useRouter } from 'vue-router'

export function useGenerateResumeFlow() {
  const router = useRouter()

  const state = ref<GenerateFlowState>({
    requestId: null,
    aiModelId: null,
    selectedAdaptationLevel: null,
    wizardStep: 'vacancy',
    languageMode: 'ENGLISH_ONLY',
    adaptationSelection: 'BALANCED',
    includeCoverLetter: false,
    isLoading: false,
    errorMessage: null
  })

  const isStepValid = computed(() => {
    // Basic validation per step
    return true // Fine-grained validation done in individual step forms
  })

  /** Create generation request and move to settings */
  async function submitVacancy(data: {
    vacancyTitle: string
    vacancyDescription: string
    companyName?: string
    companyDescription?: string
    additionalComments?: string
  }) {
    state.value.isLoading = true
    state.value.errorMessage = null
    try {
      const requestId = await generateApi.createRequest({
        ...data,
        includeCoverLetter: state.value.includeCoverLetter,
        languageMode: state.value.languageMode,
        adaptationSelection: state.value.adaptationSelection,
        aiModelId: state.value.aiModelId || ''
      })
      state.value.requestId = requestId
      state.value.wizardStep = 'settings'
      router.push('/generate/settings')
    } catch (err: any) {
      state.value.errorMessage = err.message || 'Failed to create generation request.'
    } finally {
      state.value.isLoading = false
    }
  }

  /** Save settings and start generation */
  async function submitSettings(data: {
    languageMode: LanguageMode
    adaptationSelection: AdaptationSelection
    aiModelId: string
    includeCoverLetter: boolean
  }) {
    state.value.languageMode = data.languageMode
    state.value.adaptationSelection = data.adaptationSelection
    state.value.aiModelId = data.aiModelId
    state.value.includeCoverLetter = data.includeCoverLetter
    state.value.isLoading = true
    state.value.errorMessage = null

    try {
      const result = await generateApi.generate(state.value.requestId!)
      if (result.status === 'completed') {
        state.value.wizardStep = 'review'
        router.push('/generate/review')
      }
    } catch (err: any) {
      state.value.errorMessage = err.message || 'Generation failed. Please try again.'
      router.push('/generate/error')
    } finally {
      state.value.isLoading = false
    }
  }

  /** Select final adaptation level and finalize */
  async function finalizeResume(level: AdaptationLevel) {
    state.value.isLoading = true
    state.value.errorMessage = null
    try {
      state.value.selectedAdaptationLevel = level
      await generateApi.finalize(state.value.requestId!, level)
      state.value.wizardStep = 'export'
      router.push('/generate/export')
    } catch (err: any) {
      state.value.errorMessage = err.message || 'Failed to finalize resume.'
    } finally {
      state.value.isLoading = false
    }
  }

  /** Retry generation after error (same settings) */
  async function retryGeneration() {
    state.value.errorMessage = null
    state.value.isLoading = true
    try {
      const result = await generateApi.generate(state.value.requestId!)
      if (result.status === 'completed') {
        state.value.wizardStep = 'review'
        router.push('/generate/review')
      }
    } catch (err: any) {
      state.value.errorMessage = err.message || 'Generation failed. Please try again.'
    } finally {
      state.value.isLoading = false
    }
  }

  /** Go back to settings from error screen (preserves vacancy data) */
  function goToSettings() {
    state.value.wizardStep = 'settings'
    router.push('/generate/settings')
  }

  return {
    state,
    isStepValid,
    submitVacancy,
    submitSettings,
    finalizeResume,
    retryGeneration,
    goToSettings
  }
}
