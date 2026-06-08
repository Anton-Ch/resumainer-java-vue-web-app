/**
 * User Home API service.
 *
 * Fetches profile readiness summary from GET /api/user/home.
 */

export interface ProfileChecklist {
  contactDetails: boolean
  workExperience: boolean
  education: boolean
  additionalInfo: boolean
}

export interface HomeSummary {
  savedResumesCount: number
  profileStatus: string
  lastResumeId: number | null
}

export interface SavedResumeData {
  id: number
  resumeTitle: string
  vacancy: string
  company: string
  language: string
  adaptationLevel: string
  createdAt: string
  publicUrl: string
  pdfUrl: string
  coverLetter: string | null
}

export interface UserHomeSummary {
  profileReady: boolean
  profileChecklist: ProfileChecklist
  summary: HomeSummary
  lastResume: SavedResumeData | null
}

export async function fetchSummary(): Promise<UserHomeSummary> {
  const res = await fetch('/api/user/home', { credentials: 'include' })
  if (!res.ok) throw new Error('Failed to fetch home summary')
  return res.json()
}
