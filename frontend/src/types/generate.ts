/**
 * TypeScript types for the Generate Resume feature.
 * Mirrors backend DTOs for type-safe API communication.
 */

/** Language modes */
export type LanguageMode = 'ENGLISH_ONLY' | 'RUSSIAN_ONLY' | 'BILINGUAL'

/** Adaptation selections */
export type AdaptationSelection = 'MINIMAL' | 'BALANCED' | 'MAXIMUM' | 'ALL'

/** Single adaptation level for saved responses */
export type AdaptationLevel = 'MINIMAL' | 'BALANCED' | 'MAXIMUM'

/** Wizard step */
export type WizardStep = 'vacancy' | 'settings' | 'review' | 'export'

/** Generation request state */
export interface GenerateFlowState {
  requestId: string | null
  aiModelId: string | null
  selectedAdaptationLevel: AdaptationLevel | null
  wizardStep: WizardStep
  languageMode: LanguageMode
  adaptationSelection: AdaptationSelection
  includeCoverLetter: boolean
  isLoading: boolean
  errorMessage: string | null
}
