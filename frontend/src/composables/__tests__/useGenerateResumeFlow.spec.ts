/**
 * useGenerateResumeFlow composable tests.
 * Verifies finalization error propagation, state changes, and navigation.
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// ── Mocks (must be before any local imports) ───────────────────────

const mockPush = vi.fn()
const mockFinalize = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('@/services/generateResumeService', () => ({
  finalize: (...args: any[]) => mockFinalize(...args),
}))

// ── Import composable after mocks ──────────────────────────────────
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'

// Helper to get the composable's state ref for setup
function getComposable() {
  const c = useGenerateResumeFlow()
  return c
}

describe('useGenerateResumeFlow.finalizeResume()', () => {
  let composable: ReturnType<typeof useGenerateResumeFlow>

  beforeEach(() => {
    mockPush.mockClear()
    mockFinalize.mockClear()
    composable = useGenerateResumeFlow()
    // Set up the state for finalization
    composable.state.value.requestId = 'req-123'
    composable.state.value.wizardStep = 'review'
    composable.state.value.selectedAdaptationLevel = null
    composable.state.value.isLoading = false
    composable.state.value.errorMessage = null
  })

  it('calls generateApi.finalize with requestId and level', async () => {
    mockFinalize.mockResolvedValue({ resumes: [] })

    await composable.finalizeResume('BALANCED')

    expect(mockFinalize).toHaveBeenCalledWith('req-123', 'BALANCED')
  })

  it('on success: sets wizard step to export and navigates', async () => {
    mockFinalize.mockResolvedValue({ resumes: [] })

    await composable.finalizeResume('BALANCED')

    expect(composable.state.value.selectedAdaptationLevel).toBe('BALANCED')
    expect(composable.state.value.wizardStep).toBe('export')
    expect(mockPush).toHaveBeenCalledWith('/generate/export')
  })

  it('on success: resets loading state', async () => {
    mockFinalize.mockResolvedValue({ resumes: [] })

    await composable.finalizeResume('BALANCED')

    expect(composable.state.value.isLoading).toBe(false)
  })

  it('on failure: sets errorMessage, resets loading, and rethrows', async () => {
    mockFinalize.mockRejectedValue(new Error('Finalization failed'))

    await expect(composable.finalizeResume('BALANCED')).rejects.toThrow('Finalization failed')

    expect(composable.state.value.errorMessage).toBe('Finalization failed')
    expect(composable.state.value.isLoading).toBe(false)
  })

  it('on failure: rethrows conflict error for page-level handler', async () => {
    const conflictError = new Error('Finalization already in progress. Please wait for it to complete.')
    mockFinalize.mockRejectedValue(conflictError)

    await expect(composable.finalizeResume('BALANCED')).rejects.toThrow(
      'Finalization already in progress. Please wait for it to complete.'
    )
    expect(composable.state.value.errorMessage).toBe(
      'Finalization already in progress. Please wait for it to complete.'
    )
  })

  it('on failure: does NOT navigate and does NOT change wizard step', async () => {
    mockFinalize.mockRejectedValue(new Error('DB error'))

    await expect(composable.finalizeResume('BALANCED')).rejects.toThrow('DB error')

    expect(mockPush).not.toHaveBeenCalled()
    expect(composable.state.value.wizardStep).toBe('review')
  })
})
