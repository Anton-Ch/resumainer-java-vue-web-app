import type { ContactDetails, WorkExperience, Project, Education, Course, AdditionalInfo, ProfileData } from '@/types/profile'

const STORAGE_KEY = 'resumainer_profile_data'

function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2, 6)
}

function getDefaultProfile(): ProfileData {
  return {
    contactDetails: {
      fullName: '',
      professionalTitle: '',
      email: '',
      phone: '',
      location: '',
      linkedinUrl: '',
      portfolioUrl: '',
      telegram: '',
      whatsapp: ''
    },
    workExperience: [],
    projects: [],
    education: [],
    courses: [],
    additionalInfo: {
      username: '',
      defaultResumeLanguage: '',
      additionalResumeLanguage: '',
      acceptableWorkFormats: [],
      willingnessToRelocate: '',
      willingnessForBusinessTravel: '',
      skills: '',
      spokenLanguages: '',
      professionalAspirations: '',
      achievements: '',
      additionalContextForAI: '',
      dateOfBirth: '',
      citizenship: ''
    }
  }
}

function loadProfile(): ProfileData {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw) as ProfileData
      return data
    }
  } catch { /* ignore */ }
  return getDefaultProfile()
}

function saveProfile(data: ProfileData): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

// --- Contact Details ---

export function getContactDetails(): ContactDetails {
  return loadProfile().contactDetails
}

export function saveContactDetails(data: ContactDetails): void {
  const profile = loadProfile()
  profile.contactDetails = data
  saveProfile(profile)
}

// --- Work Experience ---

export function getWorkExperience(): WorkExperience[] {
  return loadProfile().workExperience
}

export function saveWorkExperienceRecord(record: WorkExperience): WorkExperience[] {
  const profile = loadProfile()
  const idx = profile.workExperience.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.workExperience[idx] = record
  } else {
    record.id = generateId()
    profile.workExperience.push(record)
  }
  saveProfile(profile)
  return profile.workExperience
}

export function deleteWorkExperienceRecord(id: string): WorkExperience[] {
  const profile = loadProfile()
  profile.workExperience = profile.workExperience.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.workExperience
}

// --- Projects ---

export function getProjects(): Project[] {
  return loadProfile().projects
}

export function saveProjectRecord(record: Project): Project[] {
  const profile = loadProfile()
  const idx = profile.projects.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.projects[idx] = record
  } else {
    record.id = generateId()
    profile.projects.push(record)
  }
  saveProfile(profile)
  return profile.projects
}

export function deleteProjectRecord(id: string): Project[] {
  const profile = loadProfile()
  profile.projects = profile.projects.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.projects
}

// --- Education ---

export function getEducation(): Education[] {
  return loadProfile().education
}

export function saveEducationRecord(record: Education): Education[] {
  const profile = loadProfile()
  const idx = profile.education.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.education[idx] = record
  } else {
    record.id = generateId()
    profile.education.push(record)
  }
  saveProfile(profile)
  return profile.education
}

export function deleteEducationRecord(id: string): Education[] {
  const profile = loadProfile()
  profile.education = profile.education.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.education
}

// --- Courses ---

export function getCourses(): Course[] {
  return loadProfile().courses
}

export function saveCourse(record: Course): Course[] {
  const profile = loadProfile()
  const idx = profile.courses.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.courses[idx] = record
  } else {
    record.id = generateId()
    profile.courses.push(record)
  }
  saveProfile(profile)
  return profile.courses
}

export function deleteCourse(id: string): Course[] {
  const profile = loadProfile()
  profile.courses = profile.courses.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.courses
}

// --- Additional Info ---

export function getAdditionalInfo(): AdditionalInfo {
  return loadProfile().additionalInfo
}

export function saveAdditionalInfo(data: AdditionalInfo): void {
  const profile = loadProfile()
  profile.additionalInfo = data
  saveProfile(profile)
}

// --- Section Status ---

export function getProfileSectionStatuses() {
  const profile = loadProfile()
  const cd = profile.contactDetails
  const contactComplete = !!(cd.fullName && cd.professionalTitle && cd.email && cd.phone && cd.location)

  const ai = profile.additionalInfo
  const additionalComplete = !!(ai.username && ai.defaultResumeLanguage && ai.additionalResumeLanguage)

  return {
    contactComplete,
    experienceCount: profile.workExperience.length,
    projectsCount: profile.projects.length,
    educationCount: profile.education.length,
    coursesCount: profile.courses.length,
    additionalComplete
  }
}

// --- Seed sample data ---

export function seedSampleData(): void {
  const profile = loadProfile()
  if (profile.workExperience.length > 0 || profile.education.length > 0) return

  profile.contactDetails = {
    fullName: 'John Doe',
    professionalTitle: 'Business Analyst, Junior Java Developer',
    email: 'johndoe@example.com',
    phone: '+7-777-777-77-77',
    location: 'Kazakhstan, Astana',
    linkedinUrl: 'https://www.linkedin.com/in/john/',
    portfolioUrl: '',
    telegram: '@johndoe',
    whatsapp: ''
  }

  profile.workExperience = [
    {
      id: 'we1',
      jobTitle: 'Junior Business Analyst',
      companyName: 'Tech Solutions Ltd',
      location: 'Astana, Kazakhstan',
      startDate: '2024-01-15',
      endDate: '',
      currentlyWorkHere: true,
      description: 'Gathering and documenting business requirements. Creating user stories, process flows, and wireframes. Collaborating with development team on feature implementation.',
      companyUrl: 'https://techsolutions.example.com'
    },
    {
      id: 'we2',
      jobTitle: 'Intern Java Developer',
      companyName: 'Digital Innovations Inc',
      location: 'Astana, Kazakhstan',
      startDate: '2023-06-01',
      endDate: '2023-12-31',
      currentlyWorkHere: false,
      description: 'Assisted in developing REST APIs using Spring Boot. Wrote unit tests with JUnit. Participated in code reviews and agile ceremonies.',
      companyUrl: ''
    }
  ]

  profile.education = [
    {
      id: 'ed1',
      institutionName: 'Astana IT University',
      degree: 'Bachelor of Science',
      fieldOfStudy: 'Computer Science',
      startDate: '2021-09-01',
      endDate: '',
      currentlyStudying: true,
      location: 'Astana, Kazakhstan',
      comment: 'Focus on software engineering and data analysis.',
      gpa: '3.8'
    }
  ]

  profile.courses = [
    { id: 'co1', courseName: 'Java Programming Masterclass', provider: 'Udemy', startDate: '2023-03-01', endDate: '2023-06-30', credentialUrl: '', skills: 'Java, Spring, Hibernate', description: 'Comprehensive Java course' },
    { id: 'co2', courseName: 'SQL for Data Analysis', provider: 'Coursera', startDate: '2023-07-01', endDate: '2023-09-30', credentialUrl: '', skills: 'SQL, PostgreSQL, Data Modeling', description: '' },
    { id: 'co3', courseName: 'Business Analysis Fundamentals', provider: 'LinkedIn Learning', startDate: '2024-02-01', endDate: '2024-04-30', credentialUrl: '', skills: 'Requirements, UML, Agile', description: '' }
  ]

  profile.additionalInfo = {
    username: 'johndoe',
    defaultResumeLanguage: 'en',
    additionalResumeLanguage: 'ru',
    acceptableWorkFormats: ['remote', 'hybrid'],
    willingnessToRelocate: 'negotiable',
    willingnessForBusinessTravel: 'yes',
    skills: 'Business Analysis, Java, Spring, SQL, UML, Agile, REST APIs, Requirements Gathering',
    spokenLanguages: 'English C1, Russian native',
    professionalAspirations: 'To become a senior business analyst with deep technical understanding.',
    achievements: 'Successfully delivered 3 major projects ahead of schedule.',
    additionalContextForAI: 'Prefer clean structured resumes with quantifiable achievements.',
    dateOfBirth: '',
    citizenship: 'Kazakhstan'
  }

  saveProfile(profile)
}
