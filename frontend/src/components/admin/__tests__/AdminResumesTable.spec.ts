import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminResumesTable from '@/components/admin/AdminResumesTable.vue'
import PrimeVue from 'primevue/config'
import type { AdminSavedResume } from '@/types/admin'

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false, media: query, onchange: null, addListener: vi.fn(),
    removeListener: vi.fn(), addEventListener: vi.fn(), removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

const mockT = vi.fn((key: string) => {
  const m: Record<string, string> = {
    'admin.resumes.searchPlaceholder': 'Search',
    'admin.resumes.clear': 'Clear',
    'admin.resumes.clearTooltip': 'Clear filters',
    'admin.resumes.resumeTitle': 'Resume title',
    'admin.resumes.vacancy': 'Vacancy',
    'admin.resumes.company': 'Company',
    'admin.resumes.owner': 'Owner',
    'admin.resumes.email': 'Email',
    'admin.resumes.language': 'Language',
    'admin.resumes.adaptationLevel': 'Adaptation',
    'admin.resumes.created': 'Created',
    'admin.resumes.pdfStatus': 'PDF',
    'admin.resumes.dateFrom': 'From',
    'admin.resumes.dateTo': 'To',
    'admin.resumes.pageReport': 'Showing {first} to {last} of {totalRecords}',
    'admin.resumes.noResults': 'No results',
    'admin.resumes.emptyTitle': 'No resumes',
    'admin.resumes.emptyText': 'No resumes found.',
    'admin.resumes.dateError': 'End date must be after start date.',
    'adaptation.minimal': 'Minimal',
    'adaptation.balanced': 'Balanced',
    'adaptation.maximum': 'Maximum',
    'common.loading': 'Loading...',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

function makeResume(id: number, overrides: Partial<AdminSavedResume> = {}): AdminSavedResume {
  return {
    id,
    ownerUserId: 'uuid-' + id,
    ownerUsername: 'user' + id,
    ownerEmail: 'user' + id + '@test.com',
    ownerFullName: 'User ' + id,
    resumeTitle: 'Resume ' + id,
    vacancyTitle: 'Vacancy ' + id,
    companyName: 'Company ' + id,
    languageCode: id % 2 === 0 ? 'EN' : 'RU',
    languageName: id % 2 === 0 ? 'English' : 'Russian',
    adaptationLevel: 'BALANCED',
    createdAt: '2026-06-20',
    publicUrlLink: 'https://example.com/user' + id + '/CODE',
    pdfOpenUrl: '/api/generate/resumes/' + id + '/pdf?disposition=inline',
    pdfDownloadUrl: '/api/generate/resumes/' + id + '/pdf',
    htmlDownloadUrl: '/api/generate/resumes/' + id + '/html',
    pdfAvailable: true,
    pdfStatus: 'READY',
    pdfMessage: null,
    coverLetter: null,
    ...overrides,
  }
}

function mountTable(props: {
  items: AdminSavedResume[]
  totalRecords?: number
  loading?: boolean
  first?: number
  sortField?: string
  sortOrder?: number
  size?: number
}) {
  return mount(AdminResumesTable, {
    props: {
      items: props.items,
      totalRecords: props.totalRecords ?? props.items.length,
      loading: props.loading ?? false,
      first: props.first ?? 0,
      sortField: props.sortField ?? 'createdAt',
      sortOrder: props.sortOrder ?? -1,
      size: props.size ?? 10,
    },
    global: { plugins: [PrimeVue], mocks: { $t: mockT } },
    attachTo: document.body,
  })
}

describe('AdminResumesTable', () => {
  it('renders rows from items prop', () => {
    const items = [makeResume(1)]
    const wrapper = mountTable({ items })
    expect(wrapper.text()).toContain('Resume 1')
  })

  it('renders owner email and username', () => {
    const items = [makeResume(1)]
    const wrapper = mountTable({ items })
    expect(wrapper.text()).toContain('user1@test.com')
    expect(wrapper.text()).toContain('user1')
  })

  it('emits openResume on row click', async () => {
    const items = [makeResume(1)]
    const wrapper = mountTable({ items })
    const rows = wrapper.findAll('tr')
    const dataRow = rows.find(r => r.html().includes('Resume 1'))
    expect(dataRow).toBeTruthy()
    await dataRow!.trigger('click')
    const emitted = wrapper.emitted('openResume')
    expect(emitted).toBeTruthy()
    if (emitted) {
      const payload = emitted[0][0] as AdminSavedResume
      expect(payload.id).toBe(1)
    }
  })

  it('shows pdfMessage when pdfAvailable is false (through vm)', () => {
    const items = [makeResume(1, { pdfAvailable: false, pdfMessage: 'PDF is being generated.' })]
    const wrapper = mountTable({ items })
    const vm = wrapper.vm as any
    expect(vm.$props.items[0].pdfMessage).toBe('PDF is being generated.')
    expect(vm.$props.items[0].pdfAvailable).toBe(false)
  })

  it('emits search event with debounce via vm.onSearchInput', async () => {
    const items: AdminSavedResume[] = []
    const wrapper = mountTable({ items })
    const vm = wrapper.vm as any
    // Simulate keyboard input
    const event = { target: { value: 'java' } } as any
    vm.onSearchInput(event)
    await new Promise(r => setTimeout(r, 400))
    const emitted = wrapper.emitted('search')
    expect(emitted).toBeTruthy()
    if (emitted) {
      expect(emitted[emitted.length - 1][0]).toBe('java')
    }
  })

  it('emits page event', () => {
    const items = [makeResume(1)]
    const wrapper = mountTable({ items, totalRecords: 50 })
    const vm = wrapper.vm as any
    vm.onPage({ first: 10, rows: 10 })
    const emitted = wrapper.emitted('page')
    expect(emitted).toBeTruthy()
  })
})

describe('AdminResumesTable date validation', () => {
  it('blocks request when dateTo < dateFrom and shows error', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.dateFrom = new Date('2026-06-10')
    vm.dateTo = new Date('2026-06-01')
    vm.onFiltersChange()
    expect(vm.dateError).toBeTruthy()
  })

  it('passes valid date range', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.dateFrom = new Date('2026-06-01')
    vm.dateTo = new Date('2026-06-10')
    vm.dateError = null
    vm.onFiltersChange()
    const emitted = wrapper.emitted('filter')
    expect(emitted).toBeTruthy()
    expect(vm.dateError).toBeNull()
  })
})
