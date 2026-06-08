/**
 * Profile REST API service.
 *
 * Provides methods for all 6 profile sections:
 * Contact Details, Work Experience, Education, Projects,
 * Courses & Certificates, Additional Info.
 *
 * All requests include credentials (session cookie) for authentication.
 * Error handling: non-OK responses throw descriptive Error messages.
 */

import type {
  ContactDetails,
  WorkExperience,
  Education,
  Project,
  Course,
  CoursePage,
  SectionStatus
} from '@/types/profile'

const BASE = '/api/profile'

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, { credentials: 'include', ...options })
  if (!res.ok) {
    const body = await res.text()
    let message = `HTTP ${res.status}`
    try {
      const json = JSON.parse(body)
      message = json.message || message
    } catch { /* use default message */ }
    throw new Error(message)
  }
  // 204 No Content
  if (res.status === 204) return undefined as T
  return res.json()
}

// ========================================================================
// Section Status
// ========================================================================

export function fetchSectionStatus(): Promise<SectionStatus> {
  return request<SectionStatus>(`${BASE}/status`)
}

// ========================================================================
// Contact Details
// ========================================================================

export function fetchContactDetails(): Promise<ContactDetails> {
  return request<ContactDetails>(`${BASE}/contact`)
}

export function updateContactDetails(data: ContactDetails): Promise<ContactDetails> {
  return request<ContactDetails>(`${BASE}/contact`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

// ========================================================================
// Work Experience
// ========================================================================

export function fetchExperiences(): Promise<WorkExperience[]> {
  return request<WorkExperience[]>(`${BASE}/experience`)
}

export function createExperience(data: Omit<WorkExperience, 'id'>): Promise<WorkExperience> {
  return request<WorkExperience>(`${BASE}/experience`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function updateExperience(id: number, data: WorkExperience): Promise<WorkExperience> {
  return request<WorkExperience>(`${BASE}/experience/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function deleteExperience(id: number): Promise<void> {
  return request<void>(`${BASE}/experience/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Education
// ========================================================================

export function fetchEducations(): Promise<Education[]> {
  return request<Education[]>(`${BASE}/education`)
}

export function createEducation(data: Omit<Education, 'id'>): Promise<Education> {
  return request<Education>(`${BASE}/education`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function updateEducation(id: number, data: Education): Promise<Education> {
  return request<Education>(`${BASE}/education/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function deleteEducation(id: number): Promise<void> {
  return request<void>(`${BASE}/education/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Projects
// ========================================================================

export function fetchProjects(): Promise<Project[]> {
  return request<Project[]>(`${BASE}/projects`)
}

export function createProject(data: Omit<Project, 'id'>): Promise<Project> {
  return request<Project>(`${BASE}/projects`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function updateProject(id: number, data: Project): Promise<Project> {
  return request<Project>(`${BASE}/projects/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function deleteProject(id: number): Promise<void> {
  return request<void>(`${BASE}/projects/${id}`, { method: 'DELETE' })
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
  return request<CoursePage>(`${BASE}/courses${qs ? '?' + qs : ''}`)
}

export function createCourse(data: Omit<Course, 'id'>): Promise<Course> {
  return request<Course>(`${BASE}/courses`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function updateCourse(id: number, data: Course): Promise<Course> {
  return request<Course>(`${BASE}/courses/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function deleteCourse(id: number): Promise<void> {
  return request<void>(`${BASE}/courses/${id}`, { method: 'DELETE' })
}

// ========================================================================
// Additional Info
// ========================================================================

export function fetchAdditionalInfo(): Promise<Record<string, unknown>> {
  return request<Record<string, unknown>>(`${BASE}/additional`)
}

export function updateAdditionalInfo(data: Record<string, unknown>): Promise<Record<string, unknown>> {
  return request<Record<string, unknown>>(`${BASE}/additional`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}
