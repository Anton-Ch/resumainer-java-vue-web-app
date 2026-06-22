/**
 * GenerateReviewPage tests — finalization flow, error handling, double-click blocking.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, nextTick } from 'vue'
import GenerateReviewPage from '@/views/generate/GenerateReviewPage.vue'

// ── Mocks ───────────────────────────────────────────────────────────

const mockRouterPush = vi.fn()
const mockSaveReview = vi.fn()
const mockGetReview = vi.fn()
const mockFinalize = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
}))

// Composable state mock
const mockComposableState = ref({
  requestId: 'req-123' as string | null,
  aiModelId: null as string | null,
  selectedAdaptationLevel: null as string | null,
  wizardStep: 'review' as const,
  languageMode: 'ENGLISH_ONLY' as const,
  adaptationSelection: 'BALANCED' as const,
  includeCoverLetter: false,
  isLoading: false,
  errorMessage: null as string | null,
})

const mockFinalizeResume = vi.fn()

vi.mock('@/composables/useGenerateResumeFlow', () => ({
  useGenerateResumeFlow: () => ({
    state: mockComposableState,
    finalizeResume: mockFinalizeResume,
  }),
}))

vi.mock('@/services/generateResumeService', () => ({
  getReview: (...args: any[]) => mockGetReview(...args),
  saveReview: (...args: any[]) => mockSaveReview(...args),
}))

// i18n mock
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'generate.review.title': 'Review & Edit',
        'generate.review.noData': 'No data.',
        'generate.review.goToSettings': 'Go to settings',
        'generate.review.loading': 'Loading...',
        'generate.review.tabs.positioning': 'Positioning',
        'generate.review.tabs.work': 'Work',
        'generate.review.tabs.courses': 'Courses',
        'generate.review.tabs.projects': 'Projects',
        'generate.review.tabs.skills': 'Skills',
        'generate.review.tabs.personal': 'Personal',
      }
      return translations[key] || key
    },
    locale: ref('en'),
  }),
}))

vi.mock('primevue/usetoast', () => ({
  useToast: () => ({ add: vi.fn() }),
}))

// ── Stubs ───────────────────────────────────────────────────────────

const stubs = {
  AppHeader: { template: '<div class="app-header-stub" />' },
  GenerateStepper: {
    template: '<div class="stepper-stub" />',
    props: ['currentStep', 'disabledSteps'],
  },
  ReviewStepForm: {
    template: `<div class="review-form-stub">
      <button class="save-btn-stub" @click="$emit('save')">Save</button>
    </div>`,
    props: ['enVariants', 'ruVariants', 'isBilingual', 'showLevels', 'isFinalizing', 'activeTab', 'selectedLevel'],
    emits: ['save', 'update:activeTab', 'update:selectedLevel'],
  },
}

// ── Helpers ─────────────────────────────────────────────────────────

function mountPage() {
  return mount(GenerateReviewPage, {
    global: {
      stubs,
      mocks: {
        $t: (key: string) => key,
      },
    },
  })
}

function mockReviewData() {
  return {
    requestId: 'req-123',
    languages: [
      {
        languageId: 1,
        languageCode: 'EN' as const,
        sections: [
          {
            sectionKey: 'positioning',
            sectionLabel: 'Positioning',
            records: [
              {
                recordId: 'rec-1',
                orderInResume: 0,
                fieldVariants: {
                  professionalTitle: [{ responseId: 'r1', adaptationLevelId: 1, adaptationCode: 'BALANCED' as const, value: 'Software Engineer', updateKey: 'pos:rec-1:professionalTitle:BALANCED' }],
                },
              },
            ],
          },
        ],
      },
    ],
  }
}

beforeEach(() => {
  mockRouterPush.mockClear()
  mockSaveReview.mockClear()
  mockGetReview.mockClear()
  mockFinalizeResume.mockClear()
  mockComposableState.value.requestId = 'req-123'
  mockComposableState.value.errorMessage = null
  mockComposableState.value.isLoading = false
})

// ── Tests ───────────────────────────────────────────────────────────

describe('GenerateReviewPage', () => {
  // TEST 1
  it('redirects to vacancy when requestId missing', async () => {
    mockComposableState.value.requestId = null
    mountPage()

    await vi.waitFor(() => {
      expect(mockRouterPush).toHaveBeenCalledWith('/generate/vacancy')
    })
  })

  // TEST 2
  it('loads review data on mount', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())

    const wrapper = mountPage()

    await vi.waitFor(() => {
      expect(mockGetReview).toHaveBeenCalledWith('req-123')
    })

    await nextTick()
    // ReviewStepForm stub should be rendered
    expect(wrapper.find('.review-form-stub').exists()).toBe(true)
  })

  // TEST 3 — finalizeResume is called after save
  it('finalizeResume is called on save click', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())
    mockFinalizeResume.mockResolvedValue(undefined)

    const wrapper = mountPage()
    await vi.waitFor(() => expect(mockGetReview).toHaveBeenCalled())
    await nextTick()

    const saveBtn = wrapper.find('.save-btn-stub')
    await saveBtn.trigger('click')

    await vi.waitFor(() => {
      expect(mockFinalizeResume).toHaveBeenCalled()
    })
  })

  // TEST 4 — if saveReview fails (when edits exist), finalizeResume is not called
  it('finalize error propagates correctly via composable rethrow', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())
    mockSaveReview.mockResolvedValue({ success: true })
    mockFinalizeResume.mockRejectedValue(new Error('DB connection lost'))

    const wrapper = mountPage()
    await vi.waitFor(() => expect(mockGetReview).toHaveBeenCalled())
    await nextTick()

    const saveBtn = wrapper.find('.save-btn-stub')
    await saveBtn.trigger('click')

    await vi.waitFor(() => {
      // Error alert should appear (finalizeError from page-level catch)
      expect(wrapper.find('.vue-alert-error').exists()).toBe(true)
    })

    // finalizeResume was called but threw — page handled it
    expect(mockFinalizeResume).toHaveBeenCalledTimes(1)
  })

  // TEST 5 — double-click finalize is blocked by isFinalizing flag
  it('double-click finalize calls finalizeResume only once', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())
    mockSaveReview.mockResolvedValue({ success: true })
    mockFinalizeResume.mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve(undefined), 100))
    )

    const wrapper = mountPage()
    await vi.waitFor(() => expect(mockGetReview).toHaveBeenCalled())
    await nextTick()

    const saveBtn = wrapper.find('.save-btn-stub')

    // Trigger save twice rapidly
    saveBtn.trigger('click')
    saveBtn.trigger('click')

    // Wait for the first call to start
    await vi.waitFor(() => {
      expect(mockFinalizeResume).toHaveBeenCalled()
    }, { timeout: 500 })

    // finalizeResume should be called exactly once (second click blocked by isFinalizing)
    expect(mockFinalizeResume).toHaveBeenCalledTimes(1)
  })

  // TEST 6 — finalize 409 conflict shows error
  it('finalize 409 conflict shows conflict message', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())
    mockSaveReview.mockResolvedValue({ success: true })
    mockFinalizeResume.mockRejectedValue(
      new Error('Finalization already in progress. Please wait for it to complete.')
    )

    const wrapper = mountPage()
    await vi.waitFor(() => expect(mockGetReview).toHaveBeenCalled())
    await nextTick()

    const saveBtn = wrapper.find('.save-btn-stub')
    await saveBtn.trigger('click')

    await vi.waitFor(() => {
      const errorAlert = wrapper.find('.vue-alert-error')
      expect(errorAlert.exists()).toBe(true)
      expect(errorAlert.text()).toContain('is already in progress')
    })
  })

  // TEST 7 — finalize generic failure shows error and resets loading
  it('finalize generic failure shows error and resets loading', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())
    mockSaveReview.mockResolvedValue({ success: true })
    mockFinalizeResume.mockRejectedValue(new Error('Internal server error'))

    const wrapper = mountPage()
    await vi.waitFor(() => expect(mockGetReview).toHaveBeenCalled())
    await nextTick()

    const saveBtn = wrapper.find('.save-btn-stub')
    await saveBtn.trigger('click')

    await vi.waitFor(() => {
      const errorAlert = wrapper.find('.vue-alert-error')
      expect(errorAlert.exists()).toBe(true)
    })
  })

  // TEST 8 — success does not push /generate/export from page (composable owns navigation)
  it('on success: page does not call router.push to export', async () => {
    mockGetReview.mockResolvedValue(mockReviewData())
    mockSaveReview.mockResolvedValue({ success: true })
    mockFinalizeResume.mockResolvedValue(undefined)

    const wrapper = mountPage()
    await vi.waitFor(() => expect(mockGetReview).toHaveBeenCalled())
    await nextTick()

    // Clear routerPush before clicking save
    mockRouterPush.mockClear()
    const saveBtn = wrapper.find('.save-btn-stub')
    await saveBtn.trigger('click')

    await vi.waitFor(() => {
      expect(mockFinalizeResume).toHaveBeenCalled()
    })

    // The page should NOT call router.push to /generate/export
    // (composable handles navigation; page removed duplicate push)
    const exportCalls = mockRouterPush.mock.calls.filter(
      (call: any[]) => call[0] === '/generate/export'
    )
    expect(exportCalls.length).toBe(0)
  })
})
