/**
 * TypeScript interfaces for the User Profile feature.
 * Matches backend DTOs and model classes.
 * Field names follow BA data dictionary conventions.
 */

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
  id: number
  jobTitle: string
  companyName: string
  location: string
  startDate: string      // ISO date string
  endDate: string | null
  currentlyWorkHere: boolean
  description: string
  companyUrl: string
}

export interface Education {
  id: number
  institutionName: string
  degree: string
  fieldOfStudy: string
  startDate: string
  endDate: string | null
  currentlyStudying: boolean
  location: string
  description: string    // BA field name (was "comment" in spec)
  gpaGrade: string       // BA field name (was "gpa" in spec)
}

export interface Project {
  id: number
  projectName: string
  role: string
  startDate: string
  endDate: string | null
  isOngoing: boolean
  description: string
  location: string
  projectUrl: string
}

export interface Course {
  id: number
  courseName: string
  provider: string
  startDate: string
  endDate: string | null
  credentialUrl: string
  courseFocus: string    // BA field name (was "skills" in prototype)
  description: string
}

export interface AdditionalInfo {
  username: string
  defaultResumeLanguage: number | null
  additionalResumeLanguage: number | null
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

export interface SectionStatus {
  contact: 'completed' | 'incomplete'
  experience: { count: number; label: string }
  education: { count: number; label: string }
  projects: { count: number; label: string }
  courses: { count: number; label: string }
  additional: 'completed' | 'incomplete'
}

export interface CoursePage {
  content: Course[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
