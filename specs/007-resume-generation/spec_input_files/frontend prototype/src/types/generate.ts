export type ResumeLanguageMode = 'English only' | 'Russian only' | 'Bilingual'
export type AdaptationLevel = 'Minimal' | 'Balanced' | 'Maximum'
export type AdaptationSelection = AdaptationLevel | 'All'
export type ResumeLanguage = 'EN' | 'RU'

export interface GenerateResumeFormState {
  vacancyTitle: string
  vacancyDescription: string
  companyName: string
  companyDescription: string
  additionalComments: string
  languageMode: ResumeLanguageMode
  adaptationSelection: AdaptationSelection
  includeCoverLetter: boolean
}

export interface GeneratedExperience {
  sourceId: string
  jobTitle: string
  companyName: string
  location: string
  dateRange: string
  description: string
  bullets: string[]
}

export interface GeneratedEducation {
  sourceId: string
  institutionName: string
  degree: string
  fieldOfStudy: string
  dateRange: string
  description: string
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
  bullets: string[]
}

export interface GeneratedSkillGroup {
  groupName: string
  skills: string[]
}

export interface GeneratedPersonalInfo {
  location: string
  spokenLanguages: string
  willingnessToRelocate: string
  willingnessForBusinessTrips: string
  citizenship: string
  dateOfBirth: string
  workFormats: string
  gpaGrade: string | null
}

export interface GeneratedVariant {
  language: ResumeLanguage
  adaptationLevel: AdaptationLevel
  professionalTitle: string
  valueLine: string
  professionalSummary: string
  professionalAspirations: string
  personalInfo?: GeneratedPersonalInfo
  workExperience: GeneratedExperience[]
  education: GeneratedEducation[]
  courses: GeneratedCourse[]
  projects: GeneratedProject[]
  skills: GeneratedSkillGroup[]
  coverLetter?: string
}
