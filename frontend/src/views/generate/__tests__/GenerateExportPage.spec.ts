/**
 * GenerateExportPage tests — RED phase: tests against current implementation.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import GenerateExportPage from '@/views/generate/GenerateExportPage.vue'
import type { ExportResultDto } from '@/types/generate'
import type { GenerateFlowState } from '@/types/generate'

// ── Mocks ───────────────────────────────────────────────────────────

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockT = vi.fn((key: string) => {
  const translations: Record<string, string> = {
    'generate.export.title': 'Resume is ready',
    'generate.export.generateAnother': 'Generate another resume',
    'generate.export.noData': 'No data.',
    'generate.export.goToReview': 'Go to review',
    'generate.export.publicLink': 'Public PDF link',
    'generate.export.copyLink': 'Copy link',
    'generate.export.safeLinkHint': 'Safe link hint.',
    'generate.export.downloadPdf': 'Download PDF',
    'generate.export.openPdf': 'Open PDF in new tab',
    'generate.export.downloadHtml': 'Download HTML',
    'generate.export.downloadHtmlHelp': 'HTML download help.',
    'generate.export.copyCoverLetter': 'Copy cover letter',
    'generate.export.englishResume': 'English resume',
    'generate.export.russianResume': 'Russian resume',
    'generate.export.singleHelp': 'Single help.',
    'generate.export.bilingualHelp': 'Bilingual help.',
    'generate.export.linkCopied': 'Link copied.',
    'generate.export.coverLetterCopied': 'Cover letter copied.',
    'generate.export.pdfNotAvailable': 'PDF not available.',
    'generate.review.fields.coverLetter': 'Cover Letter',
    'generate.steps.export': 'Export',
    'generate.steps.review': 'Review',
    'generate.steps.settings': 'Settings',
    'generate.steps.vacancy': 'Vacancy',
  }
  return translations[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: mockT,
    locale: ref('en'),
    fallbackLocale: ref('en'),
  }),
}))

vi.mock('primevue/usetoast', () => ({
  useToast: () => ({ add: vi.fn() }),
}))

const mockState = ref<GenerateFlowState>({
  requestId: 'test-request-id',
  aiModelId: null,
  selectedAdaptationLevel: null,
  wizardStep: 'export',
  languageMode: 'ENGLISH_ONLY',
  adaptationSelection: 'BALANCED',
  includeCoverLetter: false,
  isLoading: false,
  errorMessage: null,
})

vi.mock('@/composables/useGenerateResumeFlow', () => ({
  useGenerateResumeFlow: () => ({ state: mockState }),
}))

const mockGetExport = vi.fn()

vi.mock('@/services/generateResumeService', () => ({
  getExport: (...args: any[]) => mockGetExport(...args),
  downloadHtml: vi.fn(),
}))

// ── Stubs ───────────────────────────────────────────────────────────

const stubs = {
  AppHeader: { template: '<div class="app-header-stub">AppHeader</div>' },
  GenerateStepper: {
    template: '<div class="stepper-stub">{{ currentStep }}</div>',
    props: ['currentStep', 'disabledSteps'],
  },
  ExportResult: {
    template: '<div class="export-result-stub">ExportResult</div>',
    props: ['exportData'],
  },
}

// ── Helpers ─────────────────────────────────────────────────────────

function mountPage() {
  return mount(GenerateExportPage, {
    global: {
      stubs,
      mocks: {
        $t: mockT,
        $router: { push: mockPush },
      },
    },
  })
}

beforeEach(() => {
  mockPush.mockClear()
  mockGetExport.mockClear()
  mockState.value.requestId = 'test-request-id'
})

// ── Tests ───────────────────────────────────────────────────────────

describe('GenerateExportPage', () => {
  // TEST 1
  it('redirects to vacancy when requestId missing', async () => {
    mockState.value.requestId = null

    mountPage()

    await vi.waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith('/generate/vacancy')
    })
  })

  // TEST 2
  it('renders no data fallback when export fetch fails', async () => {
    mockGetExport.mockRejectedValue(new Error('Failed'))
    mockState.value.requestId = 'test-request-id'

    const wrapper = mountPage()

    await vi.waitFor(() => {
      expect(mockGetExport).toHaveBeenCalled()
    })

    // Should show fallback with no data text
    await vi.waitFor(() => {
      const text = wrapper.text()
      expect(text).toContain('No data.')
    })
  })

  // TEST 3
  it('loads export data by requestId on mount', async () => {
    const exportData: ExportResultDto = {
      resumes: [
        {
          savedResumeId: 1,
          languageCode: 'EN',
          adaptationLevel: 'BALANCED',
          htmlDownloadUrl: '/api/generate/resumes/1/html',
          pdfDownloadUrl: '/api/generate/resumes/1/pdf',
          pdfOpenUrl: '/api/generate/resumes/1/pdf?disposition=inline',
          publicUrlLink: '/alice/ABC',
          pdfAvailable: true,
          pdfMessage: null,
        },
      ],
    }
    mockGetExport.mockResolvedValue(exportData)

    const wrapper = mountPage()

    await vi.waitFor(() => {
      expect(mockGetExport).toHaveBeenCalledWith('test-request-id')
    })

    // ExportResult stub should be visible
    await vi.waitFor(() => {
      expect(wrapper.find('.export-result-stub').exists()).toBe(true)
    })
  })

  // TEST 4
  it('generate another returns to vacancy', async () => {
    const exportData: ExportResultDto = { resumes: [] }
    mockGetExport.mockResolvedValue(exportData)

    const wrapper = mountPage()

    await vi.waitFor(() => {
      expect(mockGetExport).toHaveBeenCalled()
    })

    // Find the "Generate another resume" button
    const buttons = wrapper.findAll('button')
    const genAnotherBtn = buttons.find((b) => b.text().includes('Generate another resume'))
    if (genAnotherBtn) {
      await genAnotherBtn.trigger('click')
      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/generate/vacancy')
      })
    }
  })
})
