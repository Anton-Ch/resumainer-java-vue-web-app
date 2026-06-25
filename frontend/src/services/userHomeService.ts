/**
 * User Home API service.
 *
 * Fetches profile readiness summary from GET /api/user/home.
 * Saved resume data uses canonical HomeSavedResumeDto from backend.
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
  lastResume: SavedResumeData | null
}

/**
 * Canonical HomeSavedResumeDto matching backend response shape.
 * Used by both paginated list (GET /api/resumes) and summary.lastResume.
 */
export interface SavedResumeData {
  id: number
  resumeTitle: string
  vacancyTitle: string
  companyName: string | null
  languageCode: string | null
  adaptationLevel: string | null
  createdAt: string
  publicUrlLink: string | null
  pdfOpenUrl: string | null
  pdfDownloadUrl: string | null
  htmlDownloadUrl: string | null
  pdfAvailable: boolean
  pdfStatus: string | null
  pdfMessage: string | null
  coverLetter: string | null
}

export interface UserHomeSummary {
  profileReady: boolean
  profileChecklist: ProfileChecklist
  summary: HomeSummary
  /** @deprecated Root-level backward compat — use summary.lastResume instead */
  lastResume: SavedResumeData | null
}

export async function fetchSummary(): Promise<UserHomeSummary> {
  const res = await fetch('/api/user/home', { credentials: 'include' })
  if (!res.ok) throw new Error('Failed to fetch home summary')
  return res.json()
}
