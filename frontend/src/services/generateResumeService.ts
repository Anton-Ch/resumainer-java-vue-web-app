/**
 * Generate Resume API service.
 * All state-changing requests use shared httpClient for CSRF handling.
 */
import { apiRequest } from './httpClient'
import type {
  GenerationReviewDto,
  GenerationReviewUpdateDto,
  ExportResultDto
} from '@/types/generate'

const BASE = '/api/generate'

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

/** Download HTML (authenticated, owner-scoped). Uses backend-provided URL from export DTO. */
export async function downloadHtmlByUrl(htmlDownloadUrl: string): Promise<Blob> {
  if (!htmlDownloadUrl) throw new Error('No HTML download URL available')
  const response = await fetch(htmlDownloadUrl, { credentials: 'include' })
  if (!response.ok) throw new Error('Failed to download HTML')
  return response.blob()
}

/** Download PDF (authenticated, owner-scoped). Uses backend-provided URL from export DTO. Feature 008. */
export async function downloadPdfByUrl(pdfDownloadUrl: string): Promise<Blob> {
  if (!pdfDownloadUrl) throw new Error('No PDF download URL available')
  const response = await fetch(pdfDownloadUrl, { credentials: 'include' })
  if (!response.ok) throw new Error('Failed to download PDF')
  return response.blob()
}

/** Open PDF inline in new tab. Uses backend-provided URL from export DTO. Feature 008. */
export function openPdfByUrl(pdfOpenUrl: string): void {
  if (!pdfOpenUrl) throw new Error('No PDF open URL available')
  window.open(pdfOpenUrl, '_blank', 'noopener,noreferrer')
}
