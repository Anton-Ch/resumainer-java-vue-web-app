export interface ContactDetails {
  fullName: string
  professionalTitle: string
  email: string
  phone: string
  location: string
  linkedinUrl: string
  portfolioUrl: string
  telegram: string
  whatsapp: string
}

export interface WorkExperience {
  id: string
  jobTitle: string
  companyName: string
  location: string
  startDate: string
  endDate: string
  currentlyWorkHere: boolean
  description: string
  companyUrl: string
}

export interface Project {
  id: string
  projectName: string
  role: string
  startDate: string
  endDate: string
  isOngoing: boolean
  description: string
  projectUrl: string
}

export interface Education {
  id: string
  institutionName: string
  degree: string
  fieldOfStudy: string
  startDate: string
  endDate: string
  currentlyStudying: boolean
  location: string
  comment: string
  gpa: string
}

export interface Course {
  id: string
  courseName: string
  provider: string
  startDate: string
  endDate: string
  credentialUrl: string
  skills: string
  description: string
}

export interface AdditionalInfo {
  username: string
  defaultResumeLanguage: string
  additionalResumeLanguage: string
  acceptableWorkFormats: string[]
  willingnessToRelocate: string
  willingnessForBusinessTravel: string
  skills: string
  spokenLanguages: string
  professionalAspirations: string
  achievements: string
  additionalContextForAI: string
  dateOfBirth: string
  citizenship: string
}

export interface ProfileData {
  contactDetails: ContactDetails
  workExperience: WorkExperience[]
  projects: Project[]
  education: Education[]
  courses: Course[]
  additionalInfo: AdditionalInfo
}

export interface SectionStatus {
  key: string
  label: string
  route: string
  statusText: string
  statusType: 'completed' | 'incomplete' | 'count' | 'no-records' | 'empty'
}
