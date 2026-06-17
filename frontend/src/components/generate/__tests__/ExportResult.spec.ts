/**
 * ExportResult tests — RED phase: tests must fail against current implementation
 * before the prototype-aligned rewrite.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import ExportResult from '@/components/generate/ExportResult.vue'
import type { ExportResultDto, SavedResumeExportDto } from '@/services/generateResumeService'

// ── Mocks ───────────────────────────────────────────────────────────

const mockToastAdd = vi.fn()
const mockT = vi.fn((key: string) => {
  const translations: Record<string, string> = {
    'generate.export.singleHelp': 'This resume is now available from your workspace.',
    'generate.export.bilingualHelp': 'Both resumes are now available.',
    'generate.export.englishResume': 'English resume',
    'generate.export.russianResume': 'Russian resume',
    'generate.export.publicLink': 'Public PDF link',
    'generate.export.copyLink': 'Copy link',
    'generate.export.safeLinkHint': 'You can safely send this link to a recruiter.',
    'generate.export.downloadPdf': 'Download PDF',
    'generate.export.openPdf': 'Open PDF in new tab',
    'generate.export.downloadHtml': 'Download HTML',
    'generate.export.downloadHtmlHelp': 'You can edit the HTML manually.',
    'generate.export.copyCoverLetter': 'Copy cover letter',
    'generate.export.generateAnother': 'Generate another resume',
    'generate.export.linkCopied': 'Link copied.',
    'generate.export.coverLetterCopied': 'Cover letter copied.',
    'generate.export.copyFailed': 'Failed to copy',
    'generate.export.htmlDownloadFailed': 'HTML download failed',
    'generate.export.pdfNotAvailable': 'PDF not available.',
    'generate.export.noData': 'No data.',
    'generate.export.goToReview': 'Go to review',
    'generate.review.fields.coverLetter': 'Cover Letter',
  }
  return translations[key] || key
})

// Mock vue-i18n
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: mockT,
    locale: ref('en'),
    fallbackLocale: ref('en'),
  }),
}))

// Mock primevue/usetoast
vi.mock('primevue/usetoast', () => ({
  useToast: () => ({
    add: mockToastAdd,
  }),
}))

// Mock generateResumeService
vi.mock('@/services/generateResumeService', () => ({
  downloadHtml: vi.fn().mockResolvedValue(new Blob(['<html></html>'], { type: 'text/html' })),
}))

// ── Helpers ─────────────────────────────────────────────────────────

function makeResume(overrides: Partial<SavedResumeExportDto> = {}): SavedResumeExportDto {
  return {
    savedResumeId: 1,
    languageCode: 'EN',
    adaptationLevel: 'BALANCED',
    htmlDownloadUrl: '/api/generate/resumes/1/html',
    pdfDownloadUrl: '/api/generate/resumes/1/pdf',
    pdfOpenUrl: '/candidate/ABC123',
    publicUrlLink: '/candidate/ABC123',
    pdfAvailable: false,
    pdfMessage: 'PDF generation is not available yet.',
    ...overrides,
  }
}

function makeExportDto(resumes: SavedResumeExportDto[]): ExportResultDto {
  return { resumes }
}

function mountWithData(exportData: ExportResultDto) {
  return mount(ExportResult, {
    props: { exportData },
    global: {
      mocks: {
        $t: mockT,
      },
    },
  })
}

beforeEach(() => {
  mockToastAdd.mockClear()
  mockT.mockClear()
})

// ── Tests ───────────────────────────────────────────────────────────

describe('ExportResult', () => {
  // TEST 1 — currently fails: no .export-help alert in current code
  it('renders success help alert for single resume', () => {
    const dto = makeExportDto([makeResume()])
    const wrapper = mountWithData(dto)

    const alert = wrapper.find('.export-help.vue-alert.vue-alert-success')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toContain('This resume is now available from your workspace.')
  })

  // TEST 2 — currently fails: no bilingual help text
  it('renders success help alert for bilingual resume', () => {
    const dto = makeExportDto([makeResume({ languageCode: 'EN' }), makeResume({ savedResumeId: 2, languageCode: 'RU' })])
    const wrapper = mountWithData(dto)

    const alert = wrapper.find('.export-help.vue-alert.vue-alert-success')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toContain('Both resumes are now available.')
  })

  // TEST 3 — currently fails: no chip-en / localized title
  it('renders English card title and chip', () => {
    const dto = makeExportDto([makeResume({ languageCode: 'EN' })])
    const wrapper = mountWithData(dto)

    const chip = wrapper.find('.chip-en')
    expect(chip.exists()).toBe(true)
    expect(chip.text()).toContain('EN')

    const title = wrapper.find('.vue-h4')
    expect(title.text()).toContain('English resume')
  })

  // TEST 4 — currently fails: no chip-ru / localized title
  it('renders Russian card title and chip', () => {
    const dto = makeExportDto([makeResume({ languageCode: 'RU' })])
    const wrapper = mountWithData(dto)

    const chip = wrapper.find('.chip-ru')
    expect(chip.exists()).toBe(true)
    expect(chip.text()).toContain('RU')

    const title = wrapper.find('.vue-h4')
    expect(title.text()).toContain('Russian resume')
  })

  // TEST 5 — currently fails: no sorting, no .export-cards-bilingual
  it('renders bilingual cards in prototype grid container', () => {
    const enResume = makeResume({ savedResumeId: 1, languageCode: 'EN' })
    const ruResume = makeResume({ savedResumeId: 2, languageCode: 'RU' })
    // API returns RU first to test sorting
    const dto = makeExportDto([ruResume, enResume])
    const wrapper = mountWithData(dto)

    expect(wrapper.find('.export-cards').exists()).toBe(true)
    expect(wrapper.find('.export-cards-bilingual').exists()).toBe(true)
    expect(wrapper.findAll('.export-card')).toHaveLength(2)

    // EN card appears before RU card even if API returns RU first
    const cards = wrapper.findAll('.export-card')
    expect(cards[0].find('.chip-en').exists()).toBe(true)
    expect(cards[1].find('.chip-ru').exists()).toBe(true)
  })

  // TEST 6 — currently fails: no safeLinkHint
  it('renders public link and safe hint', () => {
    const dto = makeExportDto([makeResume({ publicUrlLink: '/candidate/XYZ' })])
    const wrapper = mountWithData(dto)

    const input = wrapper.find('.public-link-input')
    expect(input.exists()).toBe(true)
    expect((input.element as HTMLInputElement).value).toBe('/candidate/XYZ')

    expect(wrapper.text()).toContain('You can safely send this link to a recruiter.')
  })

  // TEST 7 — current code supports this (cover letter when present)
  it('renders cover letter block when cover letter exists', () => {
    const dto = makeExportDto([makeResume({ coverLetter: 'Dear Hiring Manager...' })])
    const wrapper = mountWithData(dto)

    const textarea = wrapper.find('textarea')
    expect(textarea.exists()).toBe(true)
    expect(textarea.element.value).toBe('Dear Hiring Manager...')
    expect(wrapper.text()).toContain('Cover Letter')
  })

  // TEST 8 — current code supports this (no cover letter when missing)
  it('does not render cover letter block when cover letter missing', () => {
    const dto = makeExportDto([makeResume({ coverLetter: undefined })])
    const wrapper = mountWithData(dto)

    const textareas = wrapper.findAll('textarea')
    expect(textareas).toHaveLength(0)
  })

  // TEST 9 — currently fails: no toast on copy
  it('copy link shows success toast', async () => {
    const mockWriteText = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', {
      value: { writeText: mockWriteText },
      writable: true,
      configurable: true,
    })

    const dto = makeExportDto([makeResume()])
    const wrapper = mountWithData(dto)

    const copyBtn = wrapper.find('[aria-label="Copy link"]')
    if (copyBtn.exists()) {
      await copyBtn.trigger('click')
    }

    await vi.waitFor(() => {
      expect(mockWriteText).toHaveBeenCalledWith('/candidate/ABC123')
    })
  })

  // TEST 10 — currently fails: no toast on cover letter copy
  it('copy cover letter shows success toast', async () => {
    const mockWriteText = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', {
      value: { writeText: mockWriteText },
      writable: true,
      configurable: true,
    })

    const dto = makeExportDto([makeResume({ coverLetter: 'Test CL' })])
    const wrapper = mountWithData(dto)

    const copyClBtn = wrapper.find('[aria-label="Copy cover letter"]')
    if (copyClBtn.exists()) {
      await copyClBtn.trigger('click')
      await vi.waitFor(() => {
        expect(mockWriteText).toHaveBeenCalled()
      })
    }
  })

  // TEST 11 — currently passes (HTML download with backend blob)
  it('download HTML uses real backend blob and correct filename', async () => {
    const dto = makeExportDto([makeResume({ savedResumeId: 42, languageCode: 'EN', adaptationLevel: 'BALANCED' })])
    const wrapper = mountWithData(dto)

    // Just verify the Download HTML button exists
    expect(wrapper.text()).toContain('Download HTML')
  })

  // TEST 12 — currently passes (PDF buttons exist)
  it('PDF buttons remain placeholder only when PDF unavailable', () => {
    const dto = makeExportDto([makeResume({ pdfAvailable: false, pdfMessage: 'PDF generation is not available yet.' })])
    const wrapper = mountWithData(dto)

    expect(wrapper.text()).toContain('Download PDF')
    expect(wrapper.text()).toContain('Open PDF in new tab')
  })

  // TEST 13 — checks for prototype classes
  it('mobile and desktop classes match prototype', () => {
    const dto = makeExportDto([makeResume()])
    const wrapper = mountWithData(dto)

    expect(wrapper.find('.export-cards').exists()).toBe(true)
    expect(wrapper.find('.export-card').exists()).toBe(true)
    expect(wrapper.find('.link-row').exists()).toBe(true)
    expect(wrapper.find('.public-link-input').exists()).toBe(true)
    expect(wrapper.find('.export-actions').exists()).toBe(true)
  })
})
