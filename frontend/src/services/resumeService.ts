/**
 * Resume API service.
 *
 * Fetches paginated saved resumes from GET /api/resumes
 * and soft-deletes via DELETE /api/resumes/{id}.
 */
import type { SavedResumeData } from './userHomeService'

export interface PagedResponse {
  items: SavedResumeData[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ResumeQueryParams {
  search?: string
  language?: string
  adaptationLevel?: string
  createdDate?: string
  sort?: string
  page?: number
  size?: number
}

export async function fetchResumes(params: ResumeQueryParams = {}): Promise<PagedResponse> {
  const query = new URLSearchParams()
  if (params.search) query.set('search', params.search)
  if (params.language) query.set('language', params.language)
  if (params.adaptationLevel) query.set('adaptationLevel', params.adaptationLevel)
  if (params.createdDate) query.set('createdDate', params.createdDate)
  if (params.sort) query.set('sort', params.sort)
  if (params.page !== undefined) query.set('page', String(params.page))
  if (params.size !== undefined) query.set('size', String(params.size))

  const qs = query.toString()
  const url = `/api/resumes${qs ? '?' + qs : ''}`

  const res = await fetch(url, { credentials: 'include' })
  if (!res.ok) throw new Error('Failed to fetch resumes')
  return res.json()
}

export async function deleteResume(id: number): Promise<void> {
  const res = await fetch(`/api/resumes/${id}`, {
    method: 'DELETE',
    credentials: 'include'
  })
  if (!res.ok) throw new Error('Failed to delete resume')
}
