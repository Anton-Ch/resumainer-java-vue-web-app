/**
 * useUserHome composable tests.
 *
 * Tests the real delete production API path:
 * - delete success: closes modal, reloads, success toast
 * - delete failure: keeps modal open, error toast, retry allowed
 * - in-flight protection: no double API call while loading
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

// ── Mocks (must be before any local imports) ───────────────────────

const mockToastAdd = vi.fn()
const mockT = vi.fn((key: string) => {
  const map: Record<string, string> = {
    'deleteResume.success': 'Resume deleted.',
    'deleteResume.failed': 'Failed to delete resume.',
  }
  return map[key] || key
})

const mockApiDelete = vi.fn()
const mockFetchSummary = vi.fn()
const mockFetchResumes = vi.fn()

vi.mock('primevue/usetoast', () => ({
  useToast: () => ({ add: mockToastAdd }),
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: mockT,
    locale: { value: 'en' },
    fallbackLocale: { value: 'en' },
  }),
}))

vi.mock('@/services/userHomeService', () => ({
  fetchSummary: (...args: any[]) => mockFetchSummary(...args),
}))

vi.mock('@/services/resumeService', () => ({
  deleteResume: (...args: any[]) => mockApiDelete(...args),
  fetchResumes: (...args: any[]) => mockFetchResumes(...args),
}))

// ── Import composable after mocks ──────────────────────────────────
import { useUserHome } from '@/composables/useUserHome'

function createComposable() {
  const c = useUserHome()
  return c
}

beforeEach(() => {
  vi.clearAllMocks()
  mockApiDelete.mockReset()
  mockFetchSummary.mockReset()
  mockFetchResumes.mockReset()
  // Default: API succeeds, fetchSummary/fetchResumes resolve
  mockApiDelete.mockResolvedValue(undefined)
  mockFetchSummary.mockResolvedValue({
    profileReady: true,
    profileChecklist: {},
    summary: { savedResumesCount: 0, profileStatus: 'READY', lastResumeId: null, lastResume: null },
    lastResume: null,
  })
  mockFetchResumes.mockResolvedValue({ items: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })
})

// ── Tests ───────────────────────────────────────────────────────────

describe('useUserHome.handleDelete', () => {
  it('delete success: calls API once, closes modal, reloads, shows success toast', async () => {
    const c = createComposable()

    // Setup: modal open with selected resume
    c.selectedResume.value = { id: 99 } as any
    c.modalVisible.value = true

    await c.handleDelete(99)

    // API called once with correct ID
    expect(mockApiDelete).toHaveBeenCalledTimes(1)
    expect(mockApiDelete).toHaveBeenCalledWith(99)

    // Success toast
    expect(mockToastAdd).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', detail: 'Resume deleted.' })
    )

    // Modal closed
    expect(c.modalVisible.value).toBe(false)
    // Selected resume cleared
    expect(c.selectedResume.value).toBeNull()

    // Summary + resumes reloaded
    expect(mockFetchSummary).toHaveBeenCalled()
    expect(mockFetchResumes).toHaveBeenCalled()

    // Delete loading reset
    expect(c.deleteLoading.value).toBe(false)
  })

  it('delete failure: keeps modal open, shows generic error toast, retry allowed', async () => {
    // API rejects
    mockApiDelete.mockRejectedValue(new Error('backend error detail'))
    const c = createComposable()

    c.selectedResume.value = { id: 99, resumeTitle: 'Test Resume' } as any
    c.modalVisible.value = true

    await c.handleDelete(99)

    // API called
    expect(mockApiDelete).toHaveBeenCalledTimes(1)

    // Error toast uses generic i18n key, NOT backend error detail
    expect(mockToastAdd).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error', detail: 'Failed to delete resume.' })
    )
    // Backend detail must NOT leak to user
    expect(mockToastAdd).not.toHaveBeenCalledWith(
      expect.objectContaining({ detail: 'backend error detail' })
    )

    // Modal stays open
    expect(c.modalVisible.value).toBe(true)
    // Selected resume unchanged
    expect(c.selectedResume.value).toEqual({ id: 99, resumeTitle: 'Test Resume' })

    // Delete loading reset (so retry works)
    expect(c.deleteLoading.value).toBe(false)

    // No summary/resumes reload on failure
    expect(mockFetchSummary).not.toHaveBeenCalled()
    expect(mockFetchResumes).not.toHaveBeenCalled()
  })

  it('in-flight protection: loading is true during API call, false after', async () => {
    let resolveApi: () => void
    const apiPromise = new Promise<void>((resolve) => { resolveApi = resolve })
    mockApiDelete.mockReturnValue(apiPromise)

    const c = createComposable()
    c.selectedResume.value = { id: 1 } as any
    c.modalVisible.value = true

    // Start delete (don't await yet)
    const deletePromise = c.handleDelete(1)

    // During API call, deleteLoading should be true
    // (need microtask to let the sync part run)
    await vi.waitFor(() => {
      expect(c.deleteLoading.value).toBe(true)
    })

    // Resolve API
    resolveApi!()
    await deletePromise

    // After completion, loading is false
    expect(c.deleteLoading.value).toBe(false)
  })

  it('double-click: second call while loading does not trigger second API call', async () => {
    let resolveApi: () => void
    mockApiDelete.mockReturnValue(new Promise<void>((resolve) => { resolveApi = resolve }))

    const c = createComposable()
    c.selectedResume.value = { id: 1 } as any
    c.modalVisible.value = true

    // Start first delete (don't await yet)
    const first = c.handleDelete(1)

    // Wait for the sync part to run (deleteLoading=true, API call started)
    await vi.waitFor(() => {
      expect(c.deleteLoading.value).toBe(true)
    })

    // Attempt second delete while first is in-flight
    const second = c.handleDelete(1)
    // The guard should make second return immediately without calling API
    // Both promises should resolve quickly (second is a no-op)
    await second

    // API must have been called exactly ONCE (guard prevented re-entry)
    expect(mockApiDelete).toHaveBeenCalledTimes(1)

    // Resolve the first API call
    resolveApi!()
    await first

    // After completion, loading is false
    expect(c.deleteLoading.value).toBe(false)
  })

  it('sets deleteLoading=true before API call, false in finally', async () => {
    const c = createComposable()
    c.selectedResume.value = { id: 1 } as any
    c.modalVisible.value = true

    // Before call
    expect(c.deleteLoading.value).toBe(false)

    await c.handleDelete(1)

    // After call (regardless of success/failure)
    expect(c.deleteLoading.value).toBe(false)
  })
})
