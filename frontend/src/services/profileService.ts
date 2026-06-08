/**
 * Profile REST API service.
 *
 * Provides methods for all 6 profile sections:
 * Contact Details, Work Experience, Education, Projects,
 * Courses & Certificates, Additional Info.
 *
 * Uses shared httpClient for CSRF token handling (OWASP cookie-to-header pattern).
 */

import { apiRequest } from './httpClient'
import type {
  ContactDetails,
  WorkExperience,
  Education,
  Project,
  Course,
  CoursePage,
  ProfileSectionStatus
} from '@/types/profile'

const BASE = '/api/profile'

// ========================================================================
// Section Status
// ========================================================================

export function fetchSectionStatus(): Promise<ProfileSectionStatus> {
  return apiRequest<ProfileSectionStatus>(`${BASE}/status`)
}

// ========================================================================
// Contact Details
// ========================================================================

export function fetchContactDetails(): Promise<ContactDetails> {
  return apiRequest<ContactDetails>(`${BASE}/contact`)
}

export function updateContactDetails(data: ContactDetails): Promise<ContactDetails> {
  return apiRequest<ContactDetails>(`${BASE}/contact`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

// ========================================================================
// Work Experience
// ========================================================================

export function fetchExperiences(): Promise<WorkExperience[]> {
  return apiRequest<WorkExperience[]>(`${BASE}/experience`)
}

export function createExperience(data: Omit<WorkExperience, 'id'>): Promise<WorkExperience> {
  return apiRequest<WorkExperience>(`${BASE}/experience`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function updateExperience(id: number, data: WorkExperience): Promise<WorkExperience> {
  return apiRequest<WorkExperience>(`${BASE}/experience/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function deleteExperience(id: number): Promise<void> {
  return apiRequest<void>(`${BASE}/experience/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Education
// ========================================================================

export function fetchEducations(): Promise<Education[]> {
  return apiRequest<Education[]>(`${BASE}/education`)
}

export function createEducation(data: Omit<Education, 'id'>): Promise<Education> {
  return apiRequest<Education>(`${BASE}/education`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function updateEducation(id: number, data: Education): Promise<Education> {
  return apiRequest<Education>(`${BASE}/education/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function deleteEducation(id: number): Promise<void> {
  return apiRequest<void>(`${BASE}/education/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Projects
// ========================================================================

export function fetchProjects(): Promise<Project[]> {
  return apiRequest<Project[]>(`${BASE}/projects`)
}

export function createProject(data: Omit<Project, 'id'>): Promise<Project> {
  return apiRequest<Project>(`${BASE}/projects`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function updateProject(id: number, data: Project): Promise<Project> {
  return apiRequest<Project>(`${BASE}/projects/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function deleteProject(id: number): Promise<void> {
  return apiRequest<void>(`${BASE}/projects/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Courses & Certificates
// ========================================================================

export interface CourseQuery {
  page?: number
  size?: number
  sort?: string
  order?: string
  search?: string
  dateFrom?: string
  dateTo?: string
}

export function fetchCourses(params?: CourseQuery): Promise<CoursePage> {
  const query = new URLSearchParams()
  if (params?.page !== undefined) query.set('page', String(params.page))
  if (params?.size !== undefined) query.set('size', String(params.size))
  if (params?.sort) query.set('sort', params.sort)
  if (params?.order) query.set('order', params.order)
  if (params?.search && params.search.length >= 3) query.set('search', params.search)
  if (params?.dateFrom) query.set('dateFrom', params.dateFrom)
  if (params?.dateTo) query.set('dateTo', params.dateTo)
  const qs = query.toString()
  return apiRequest<CoursePage>(`${BASE}/courses${qs ? '?' + qs : ''}`)
}

export function createCourse(data: Omit<Course, 'id'>): Promise<Course> {
  return apiRequest<Course>(`${BASE}/courses`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function updateCourse(id: number, data: Course): Promise<Course> {
  return apiRequest<Course>(`${BASE}/courses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function deleteCourse(id: number): Promise<void> {
  return apiRequest<void>(`${BASE}/courses/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Additional Info
// ========================================================================

export function fetchAdditionalInfo(): Promise<Record<string, unknown>> {
  return apiRequest<Record<string, unknown>>(`${BASE}/additional`)
}

export function updateAdditionalInfo(data: Record<string, unknown>): Promise<Record<string, unknown>> {
  return apiRequest<Record<string, unknown>>(`${BASE}/additional`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}
