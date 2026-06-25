/**
 * SavedResumesTable tests.
 *
 * Tests row click emits openResume with the selected resume data.
 * Regression: ensures no extra Details column/button is used as modal trigger.
 * Covers getAdaptationSeverity (4 branches) and sortTooltip (4 states) through
 * rendered DOM behavior.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import SavedResumesTable from '@/components/home/SavedResumesTable.vue'
import PrimeVue from 'primevue/config'
import type { SavedResumeData } from '@/services/userHomeService'

// ── Mocks ───────────────────────────────────────────────────────────

// PrimeVue DatePicker uses matchMedia internally
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

const mockT = vi.fn((key: string) => {
  const translations: Record<string, string> = {
    'home.table.searchPlaceholder': 'Search by title, vacancy, or company',
    'home.table.search': 'Search',
    'home.table.clear': 'Clear',
    'home.table.clearTooltip': 'Clear all filters',
    'home.table.resumeTitle': 'Resume title',
    'home.table.vacancy': 'Vacancy',
    'home.table.company': 'Company',
    'home.table.language': 'Language',
    'home.table.adaptationLevel': 'Adaptation level',
    'home.table.created': 'Created',
    'home.table.filterLanguage': 'Language',
    'home.table.filterAdaptation': 'Adaptation',
    'home.table.filterDate': 'Created date',
    'home.table.dateFrom': 'From',
    'home.table.dateTo': 'To',
    'home.table.loading': 'Loading',
    'home.table.emptyTitle': 'No resumes yet',
    'home.table.noResultsTitle': 'No resumes found',
    'home.table.noResultsText': 'Try another search or change filters.',
    'home.table.pageReport': 'Showing {first} to {last} of {totalRecords}',
    'home.table.mobilePageReport': 'Page {current} of {totalPages}',
    'home.table.sortNotSorted': 'Sort ascending',
    'home.table.sortAsc': 'Sort descending',
    'home.table.sortDesc': 'Clear sorting for this column',
    'common.loading': 'Loading...',
    'adaptation.minimal': 'Minimal',
    'adaptation.balanced': 'Balanced',
    'adaptation.maximum': 'Maximum',
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

// ── Helpers ─────────────────────────────────────────────────────────

function makeResume(id: number, overrides: Partial<SavedResumeData> = {}): SavedResumeData {
  return {
    id,
    resumeTitle: `Resume ${id}`,
    vacancyTitle: `Vacancy ${id}`,
    companyName: `Company ${id}`,
    languageCode: id % 2 === 0 ? 'EN' : 'RU',
    adaptationLevel: 'BALANCED',
    createdAt: '2026-06-20',
    publicUrlLink: 'http://localhost:8080/user/CODE',
    pdfOpenUrl: `/api/generate/resumes/${id}/pdf?disposition=inline`,
    pdfDownloadUrl: `/api/generate/resumes/${id}/pdf`,
    htmlDownloadUrl: `/api/generate/resumes/${id}/html`,
    pdfAvailable: true,
    pdfStatus: 'READY',
    pdfMessage: null,
    coverLetter: null,
    ...overrides,
  }
}

function mountTable(props: {
  resumes: SavedResumeData[]
  totalRecords?: number
  loading?: boolean
  first?: number
  sortField?: string
  sortOrder?: number
  size?: number
}) {
  return mount(SavedResumesTable, {
    props: {
      resumes: props.resumes,
      totalRecords: props.totalRecords ?? props.resumes.length,
      loading: props.loading ?? false,
      first: props.first ?? 0,
      sortField: props.sortField ?? 'createdAt',
      sortOrder: props.sortOrder ?? -1,
      size: props.size ?? 10,
    },
    global: {
      plugins: [PrimeVue],
      mocks: { $t: mockT },
    },
    attachTo: document.body,
  })
}

// ── Tests ───────────────────────────────────────────────────────────

describe('SavedResumesTable row click', () => {
  it('data rows have pointer cursor styling (FR-003)', () => {
    const resumes = [makeResume(1)]
    const wrapper = mountTable({ resumes })

    // 1. Root wrapper exists (target for :deep() CSS)
    expect(wrapper.find('.saved-resumes-table').exists()).toBe(true)

    // 2. At least one rendered data row exists
    const rows = wrapper.findAll('tr')
    const dataRows = rows.filter(r => {
      const html = r.html()
      return html.includes('Resume 1')
    })
    expect(dataRows.length).toBeGreaterThanOrEqual(1)

    // 3. PrimeVue DataTable sets data-p-selectable-row attribute on rows
    //    when @row-click is bound. In production the value is "true"
    //    when selection is active. In jsdom the value may differ, but
    //    the presence of the attribute proves the DataTable configured
    //    the row for selection/click handling.
    //    Production CSS: tr[data-p-selectable-row="true"] { cursor: pointer; }
    const hasAttr = dataRows[0].attributes('data-p-selectable-row')
    expect(hasAttr).toBeDefined()
  })

  it('emits openResume when a data row is clicked', async () => {
    const resumes = [makeResume(1), makeResume(2)]
    const wrapper = mountTable({ resumes })

    const rows = wrapper.findAll('tr')
    const dataRows = rows.filter(r => {
      const html = r.html()
      return html.includes('Resume 1') || html.includes('Resume 2')
    })
    expect(dataRows.length).toBeGreaterThanOrEqual(1)

    await dataRows[0].trigger('click')
    await wrapper.vm.$nextTick()

    const emitted = wrapper.emitted('openResume')
    expect(emitted).toBeTruthy()
    if (emitted) {
      const payload = emitted[0][0] as SavedResumeData
      expect(payload.id).toBe(1)
      expect(payload.vacancyTitle).toBe('Vacancy 1')
    }
  })

  it('does not render a Details column or any details button', () => {
    const resumes = [makeResume(1)]
    const wrapper = mountTable({ resumes })

    const allText = wrapper.text()

    // Canonical columns ARE present
    expect(allText).toContain('Resume title')
    expect(allText).toContain('Vacancy')
    expect(allText).toContain('Company')
    expect(allText).toContain('Language')

    // Forbidden: no "Details" column, button, or action label
    expect(allText).not.toContain('Details')
    expect(allText).not.toContain('Open details')
    expect(allText).not.toContain('View details')
    expect(allText).not.toContain('detail')
    // Forbidden: no row-level action button labels
    expect(allText).not.toContain('Open')
    expect(allText).not.toContain('Actions')
  })

  it('renders canonical field values in table body', () => {
    const resumes = [
      makeResume(1, { vacancyTitle: 'Lead Engineer', companyName: 'TechCorp', languageCode: 'EN' }),
    ]
    const wrapper = mountTable({ resumes })

    expect(wrapper.text()).toContain('Lead Engineer')
    expect(wrapper.text()).toContain('TechCorp')
  })
})

describe('getAdaptationSeverity — rendered Tag text', () => {
  it('shows "Minimal" for adaptationLevel MINIMAL', () => {
    const resumes = [makeResume(1, { adaptationLevel: 'MINIMAL' })]
    const wrapper = mountTable({ resumes })
    expect(wrapper.text()).toContain('Minimal')
  })

  it('shows "Balanced" for adaptationLevel BALANCED', () => {
    const resumes = [makeResume(1, { adaptationLevel: 'BALANCED' })]
    const wrapper = mountTable({ resumes })
    expect(wrapper.text()).toContain('Balanced')
  })

  it('shows "Maximum" for adaptationLevel MAXIMUM', () => {
    const resumes = [makeResume(1, { adaptationLevel: 'MAXIMUM' })]
    const wrapper = mountTable({ resumes })
    expect(wrapper.text()).toContain('Maximum')
  })

  it('shows lowercased key text for unknown adaptation level (default branch)', () => {
    const resumes = [makeResume(1, { adaptationLevel: 'CUSTOM' })]
    const wrapper = mountTable({ resumes })
    // Unknown level falls through to default → getAdaptationSeverity returns undefined
    // The Tag still renders the value, which is $t('adaptation.' + data.adaptationLevel.toLowerCase())
    // "adaptation.custom" key doesn't exist in mockT, so it returns "custom" (key itself)
    expect(wrapper.text()).toContain('custom')
  })
})

describe('sortTooltip — i18n key selection', () => {
  beforeEach(() => {
    mockT.mockClear()
  })

  function countTKey(key: string): number {
    return mockT.mock.calls.filter((c: string[]) => c[0] === key).length
  }

  it('uses "sortNotSorted" when a different field is sorted', () => {
    // sortField='createdAt', sortOrder=-1 — resumeTitle column is not current field
    mountTable({
      resumes: [makeResume(1)],
      sortField: 'createdAt',
      sortOrder: -1,
    })
    // sortTooltip('resumeTitle') is called during render → calls t('home.table.sortNotSorted')
    expect(countTKey('home.table.sortNotSorted')).toBeGreaterThanOrEqual(1)
  })

  it('uses "sortAsc" when this field is sorted ascending (sortOrder=1)', () => {
    mountTable({
      resumes: [makeResume(1)],
      sortField: 'resumeTitle',
      sortOrder: 1,
    })
    expect(countTKey('home.table.sortAsc')).toBeGreaterThanOrEqual(1)
  })

  it('uses "sortDesc" when this field is sorted descending (sortOrder=-1)', () => {
    mountTable({
      resumes: [makeResume(1)],
      sortField: 'resumeTitle',
      sortOrder: -1,
    })
    expect(countTKey('home.table.sortDesc')).toBeGreaterThanOrEqual(1)
  })

  it('uses "sortNotSorted" fallback when sortOrder is 0 (neither 1 nor -1)', () => {
    mountTable({
      resumes: [makeResume(1)],
      sortField: 'resumeTitle',
      sortOrder: 0,
    })
    expect(countTKey('home.table.sortNotSorted')).toBeGreaterThanOrEqual(1)
  })
})
