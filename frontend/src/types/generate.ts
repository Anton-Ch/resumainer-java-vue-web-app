/**
 * TypeScript types for the Generate Resume feature.
 * Mirrors backend DTOs for type-safe API communication.
 */

// ── Wizard / flow state ─────────────────────────────────────────────

/** Language modes */
export type LanguageMode = 'ENGLISH_ONLY' | 'RUSSIAN_ONLY' | 'BILINGUAL'

/** Adaptation selections */
export type AdaptationSelection = 'MINIMAL' | 'BALANCED' | 'MAXIMUM' | 'ALL'

/** Single adaptation level for saved responses */
export type AdaptationLevel = 'MINIMAL' | 'BALANCED' | 'MAXIMUM'

/** Prototype-style adaptation level labels (title case) */
export type PrototypeLevel = 'Minimal' | 'Balanced' | 'Maximum'

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

// ── Backend DTO types (GET /review response) ────────────────────────

export interface GenerationReviewDto {
  requestId: string
  languages: LanguageReviewGroup[]
}

export interface LanguageReviewGroup {
  languageId: number
  languageCode: 'EN' | 'RU'
  sections: SectionReviewGroup[]
}

export interface SectionReviewGroup {
  sectionKey: string
  sectionLabel: string
  records: RecordReviewGroup[]
}

export interface RecordReviewGroup {
  recordId: string
  orderInResume: number
  fieldVariants: Record<string, AdaptationVariant[]>
  bullets?: BulletReviewItem[]   // Feature 008: structured bullet points
}

export interface BulletReviewItem {
  bulletOrder: number
  bulletText: string
  isEdited: boolean
  updateKey: string   // opaque key: "sectionKey:recordId:bulletPoints:bulletOrder"
}

export interface AdaptationVariant {
  responseId: string
  adaptationLevelId: number
  adaptationCode: 'MINIMAL' | 'BALANCED' | 'MAXIMUM'
  value: string
  updateKey: string    // Opaque key for saveReview
}

// ── Backend DTO type (PUT /review request body) ─────────────────────

export interface GenerationReviewUpdateDto {
  requestId?: string
  fieldUpdates: Record<string, string>   // updateKey -> new value
}

// ── Prototype-style view model (after adapter transformation) ───────

export interface GeneratedExperience {
  sourceId: string
  jobTitle: string
  companyName: string
  location: string
  dateRange: string
  description: string
  bullets?: BulletReviewItem[]   // Feature 008: structured bullet points
}

export interface GeneratedCourse {
  sourceId: string
  courseName: string
  provider: string
  dateRange: string
  courseFocus: string
}

export interface GeneratedProject {
  sourceId: string
  projectName: string
  role: string
  dateRange: string
  description: string
  bullets?: BulletReviewItem[]   // Feature 008: structured bullet points
}

export interface GeneratedSkillGroup {
  groupName: string
  skills: string[]        // array of skill names
}

export interface GeneratedPersonalInfo {
  location: string
  spokenLanguages: string
  willingnessToRelocate: string
  willingnessForBusinessTrips: string
  citizenship: string
  dateOfBirth: string
  workFormats: string
}

export interface GeneratedVariant {
  language: 'EN' | 'RU'
  adaptationLevel: PrototypeLevel

  professionalTitle: string
  valueLine: string
  professionalSummary: string
  professionalAspirations: string
  coverLetter?: string

  personalInfo?: GeneratedPersonalInfo
  workExperience: GeneratedExperience[]
  courses: GeneratedCourse[]
  projects: GeneratedProject[]
  skills: GeneratedSkillGroup[]

  /** Metadata for save — maps fieldPath to updateKey */
  __meta?: Record<string, string>
}

// ── Adapted review view model ───────────────────────────────────────

export interface ReviewViewModel {
  requestId: string
  enVariants: GeneratedVariant[]
  ruVariants: GeneratedVariant[]
  isBilingual: boolean
  showLevels: boolean
}

// ── Export / saved resume DTO (Feature 008) ─────────────────────────

export interface SavedResumeExportDto {
  savedResumeId: number
  languageCode: string
  adaptationLevel: string
  htmlDownloadUrl: string
  pdfDownloadUrl: string
  pdfOpenUrl: string
  publicUrlLink: string
  pdfAvailable: boolean
  pdfMessage: string | null
  coverLetter?: string
}

export interface ExportResultDto {
  resumes: SavedResumeExportDto[]
}
