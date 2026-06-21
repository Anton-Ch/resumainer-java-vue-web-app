/**
 * Generate Resume API service.
 * All state-changing requests use shared httpClient for CSRF handling.
 * PDF/public-link methods are placeholders in feat/007
 * (real PDF generation deferred to feat/008-pdf-conversion).
 */
import { apiRequest } from './httpClient'
import type { GenerationReviewDto, GenerationReviewUpdateDto } from '@/types/generate'

const BASE = '/api/generate'
const RESUME_BASE = '/api/generate/resumes'

export interface AiModelDto {
  id: string
  provider: string
  displayName: string
  modelCode: string
}

export interface GenerationRequestCreateDto {
  vacancyTitle: string
  vacancyDescription: string
  companyName?: string
  companyDescription?: string
  additionalComments?: string
  includeCoverLetter: boolean
  languageMode: string
  adaptationSelection: string
  aiModelId: string
}

export interface SavedResumeExportDto {
  savedResumeId: number
  languageCode: string
  adaptationLevel: string
  htmlDownloadUrl: string
  pdfDownloadUrl: string
  pdfOpenUrl: string
  publicUrlLink: string
  pdfAvailable: boolean
  pdfMessage: string
  coverLetter?: string
}

export interface ExportResultDto {
  resumes: SavedResumeExportDto[]
}

/** Fetch available AI models for the current user. */
export async function getAiModels(): Promise<AiModelDto[]> {
  return apiRequest<AiModelDto[]>(`${BASE}/ai-models`, { method: 'GET' })
}

/** Create a new generation request. Returns request ID. */
export async function createRequest(dto: GenerationRequestCreateDto): Promise<string> {
  return apiRequest<string>(`${BASE}/requests`, {
    method: 'POST',
    body: JSON.stringify(dto)
  })
}

/** Save generation settings to backend (must be called before generate). */
export async function saveSettings(requestId: string, settings: {
  languageMode: string
  adaptationSelection: string
  aiModelId: string
  includeCoverLetter: boolean
}): Promise<{ success: boolean }> {
  if (!requestId || requestId === 'null' || requestId === 'undefined') {
    throw new Error('Missing generation request id')
  }
  return apiRequest<{ success: boolean }>(`${BASE}/requests/${requestId}/settings`, {
    method: 'PUT',
    body: JSON.stringify(settings)
  })
}

/** Execute generation (synchronous). Settings must be saved via saveSettings() first. */
export async function generate(requestId: string): Promise<{ status: string }> {
  if (!requestId || requestId === 'null' || requestId === 'undefined') {
    throw new Error('Missing generation request id')
  }
  return apiRequest<{ status: string }>(`${BASE}/requests/${requestId}/generate`, {
    method: 'POST'
  })
}

/** Get grouped review data. */
export async function getReview(requestId: string): Promise<GenerationReviewDto> {
  if (!requestId || requestId === 'null' || requestId === 'undefined') {
    throw new Error('Missing generation request id')
  }
  return apiRequest<GenerationReviewDto>(`${BASE}/requests/${requestId}/review`, { method: 'GET' })
}

/** Save review edits. Sends updateKey → new value map. */
export async function saveReview(requestId: string, payload: GenerationReviewUpdateDto): Promise<{ success: boolean }> {
  if (!requestId || requestId === 'null' || requestId === 'undefined') {
    throw new Error('Missing generation request id')
  }
  return apiRequest<{ success: boolean }>(`${BASE}/requests/${requestId}/review`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

/** Finalize a generation request with selected adaptation level. */
export async function finalize(requestId: string, selectedAdaptationLevel: string): Promise<ExportResultDto> {
  return apiRequest<ExportResultDto>(`${BASE}/requests/${requestId}/finalize`, {
    method: 'POST',
    body: JSON.stringify({ selectedAdaptationLevel })
  })
}

/** Get export data. */
export async function getExport(requestId: string): Promise<ExportResultDto> {
  return apiRequest<ExportResultDto>(`${BASE}/requests/${requestId}/export`, { method: 'GET' })
}

/** Download HTML (authenticated, owner-scoped). */
export async function downloadHtml(savedResumeId: number): Promise<Blob> {
  const response = await fetch(`${RESUME_BASE}/${savedResumeId}/html`, {
    credentials: 'include'
  })
  if (!response.ok) throw new Error('Failed to download HTML')
  return response.blob()
}

/** Download PDF (authenticated, owner-scoped). Feature 008. */
export async function downloadPdf(savedResumeId: number): Promise<Blob> {
  const response = await fetch(`${RESUME_BASE}/${savedResumeId}/pdf`, {
    credentials: 'include'
  })
  if (!response.ok) throw new Error('Failed to download PDF')
  return response.blob()
}

/** Open PDF inline in new tab. Feature 008. */
export async function openPdf(savedResumeId: number): Promise<void> {
  const response = await fetch(`${RESUME_BASE}/${savedResumeId}/pdf?disposition=inline`, {
    credentials: 'include'
  })
  if (!response.ok) throw new Error('Failed to open PDF')
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  window.open(url, '_blank')
}
