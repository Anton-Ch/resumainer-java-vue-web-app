import { reactive, computed } from 'vue'
import type {
  GenerateResumeFormState,
  GeneratedVariant,
  AdaptationLevel,
  ResumeLanguage
} from '@/types/generate'
import { mockGenerateResume, mockSaveToPdf } from '@/services/generateMockService'

// ─── State ───────────────────────────────────────────────────────────────────

const formState = reactive<GenerateResumeFormState>({
  vacancyTitle: '',
  vacancyDescription: '',
  companyName: '',
  companyDescription: '',
  additionalComments: '',
  languageMode: 'English only',
  adaptationSelection: 'Balanced',
  includeCoverLetter: false
})

interface FlowState {
  variants: GeneratedVariant[]
  selectedSaveLevel: AdaptationLevel
  enPublicLink: string
  ruPublicLink: string | undefined
  isGenerating: boolean
  isSavingPdf: boolean
  generationDone: boolean
  pdfDone: boolean
}

const flow = reactive<FlowState>({
  variants: [],
  selectedSaveLevel: 'Balanced',
  enPublicLink: '',
  ruPublicLink: undefined,
  isGenerating: false,
  isSavingPdf: false,
  generationDone: false,
  pdfDone: false
})

// ─── Computed ────────────────────────────────────────────────────────────────

const isBilingual = computed(() => formState.languageMode === 'Bilingual')
const isAllLevels = computed(() => formState.adaptationSelection === 'All')
const generatedLanguages = computed(() => {
  const langs: ResumeLanguage[] = []
  if (formState.languageMode === 'English only' || formState.languageMode === 'Bilingual') {
    langs.push('EN')
  }
  if (formState.languageMode === 'Russian only' || formState.languageMode === 'Bilingual') {
    langs.push('RU')
  }
  return langs
})

const enVariants = computed(() => flow.variants.filter(v => v.language === 'EN'))
const ruVariants = computed(() => flow.variants.filter(v => v.language === 'RU'))

const savedLevelEnglish = computed(() => {
  if (!isAllLevels.value) return flow.variants.filter(v => v.language === 'EN')
  return flow.variants.filter(v => v.language === 'EN' && v.adaptationLevel === flow.selectedSaveLevel)
})

const savedLevelRussian = computed(() => {
  if (!isAllLevels.value) return flow.variants.filter(v => v.language === 'RU')
  return flow.variants.filter(v => v.language === 'RU' && v.adaptationLevel === flow.selectedSaveLevel)
})

// ─── Actions ─────────────────────────────────────────────────────────────────

async function startGeneration() {
  flow.isGenerating = true
  flow.generationDone = false
  flow.pdfDone = false

  // PROTOTYPE MOCK ONLY — THIS CALL WILL BE REPLACED WITH A REAL BACKEND API POST REQUEST.
  const result = await mockGenerateResume(formState)

  flow.variants = result
  flow.isGenerating = false
  flow.generationDone = true
  flow.selectedSaveLevel = formState.adaptationSelection === 'All' ? 'Balanced' : (formState.adaptationSelection as AdaptationLevel)
}

async function saveToPdf() {
  flow.isSavingPdf = true

  // PROTOTYPE MOCK ONLY — THIS CALL WILL BE REPLACED WITH A REAL HTML-TO-PDF BACKEND FLOW.
  const result = await mockSaveToPdf()

  flow.enPublicLink = result.enPublicLink
  flow.ruPublicLink = result.ruPublicLink
  flow.isSavingPdf = false
  flow.pdfDone = true
}

function resetFlow() {
  formState.vacancyTitle = ''
  formState.vacancyDescription = ''
  formState.companyName = ''
  formState.companyDescription = ''
  formState.additionalComments = ''
  formState.languageMode = 'English only'
  formState.adaptationSelection = 'Balanced'
  formState.includeCoverLetter = false
  flow.variants = []
  flow.selectedSaveLevel = 'Balanced'
  flow.enPublicLink = ''
  flow.ruPublicLink = undefined
  flow.isGenerating = false
  flow.isSavingPdf = false
  flow.generationDone = false
  flow.pdfDone = false
}

export function useGenerateResumeFlow() {
  return {
    formState,
    flow,
    isBilingual,
    isAllLevels,
    generatedLanguages,
    enVariants,
    ruVariants,
    savedLevelEnglish,
    savedLevelRussian,
    startGeneration,
    saveToPdf,
    resetFlow
  }
}
