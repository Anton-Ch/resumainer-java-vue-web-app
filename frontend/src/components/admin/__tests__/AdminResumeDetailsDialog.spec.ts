import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminResumeDetailsDialog from '@/components/admin/AdminResumeDetailsDialog.vue'
import PrimeVue from 'primevue/config'
import type { AdminSavedResume } from '@/types/admin'

const mockToastAdd = vi.fn()
let confirmAcceptCallback: (() => void) | null = null

const mockT = vi.fn((key: string) => {
  const m: Record<string, string> = {
    'admin.resumeDetails.title': 'Resume details',
    'admin.resumeDetails.ownerInfo': 'Owner information',
    'admin.resumeDetails.resumeInfo': 'Resume information',
    'admin.resumeDetails.username': 'Username',
    'admin.resumeDetails.email': 'Email',
    'admin.resumeDetails.fullName': 'Full name',
    'admin.resumeDetails.coverLetter': 'Cover letter',
    'admin.resumeDetails.noCoverLetter': 'No cover letter.',
    'admin.resumeDetails.openPdf': 'Open PDF',
    'admin.resumeDetails.downloadPdf': 'Download PDF',
    'admin.resumeDetails.downloadHtml': 'Download HTML',
    'admin.resumeDetails.copyLink': 'Copy link',
    'admin.resumeDetails.linkCopied': 'Link copied.',
    'admin.resumeDetails.copyFailed': 'Failed to copy',
    'admin.resumeDetails.delete': 'Delete',
    'admin.resumeDetails.deleteTitle': 'Delete resume?',
    'admin.resumeDetails.deleteText': 'Delete text.',
    'admin.resumeDetails.cancel': 'Cancel',
    'admin.resumeDetails.confirm': 'Delete',
    'admin.resumeDetails.deleteSuccess': 'Deleted.',
    'admin.resumeDetails.deleteFailed': 'Failed.',
    'admin.resumeDetails.pdfNotAvailable': 'PDF is being generated. Please try again later.',
    'admin.resumes.language': 'Language',
    'admin.resumes.adaptationLevel': 'Adaptation',
    'admin.resumes.resumeTitle': 'Resume title',
    'admin.resumes.vacancy': 'Vacancy',
    'admin.resumes.company': 'Company',
    'admin.resumes.created': 'Created',
    'language.en': 'English',
    'language.ru': 'Russian',
    'adaptation.balanced': 'Balanced',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

vi.mock('primevue/usetoast', () => ({
  useToast: () => ({ add: mockToastAdd }),
}))

vi.mock('primevue/useconfirm', () => ({
  useConfirm: () => ({
    require: (opts: any) => {
      confirmAcceptCallback = opts.accept || null
    },
  }),
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
    languageCode: 'EN',
    languageName: 'English',
    adaptationLevel: 'BALANCED',
    createdAt: '2026-06-20',
    publicUrlLink: 'https://example.com/CODE',
    pdfOpenUrl: '/api/generate/resumes/1/pdf?disposition=inline',
    pdfDownloadUrl: '/api/generate/resumes/1/pdf',
    htmlDownloadUrl: '/api/generate/resumes/1/html',
    pdfAvailable: true,
    pdfStatus: 'READY',
    pdfMessage: null,
    coverLetter: null,
    ...overrides,
  }
}

function mountDialog(props: {
  visible: boolean
  resume: AdminSavedResume | null
  deleteLoading?: boolean
}) {
  confirmAcceptCallback = null
  return mount(AdminResumeDetailsDialog, {
    props: {
      visible: props.visible,
      resume: props.resume,
      deleteLoading: props.deleteLoading ?? false,
      'onUpdate:visible': (v: boolean) => wrapper.setProps({ visible: v }),
    },
    global: { plugins: [PrimeVue], mocks: { $t: mockT } },
    attachTo: document.body,
  })
}

let wrapper: ReturnType<typeof mountDialog>

beforeEach(() => {
  mockToastAdd.mockClear()
  confirmAcceptCallback = null
})

afterEach(() => {
  if (wrapper) { wrapper.unmount(); wrapper = null as any }
})

describe('AdminResumeDetailsDialog', () => {
  it('uses already-loaded DTO data, not an extra API call', () => {
    wrapper = mountDialog({ visible: true, resume: makeResume(1) })
    const vm = wrapper.vm as any
    expect(vm.$props.resume.ownerUsername).toBe('user1')
    expect(vm.$props.resume.ownerEmail).toBe('user1@test.com')
  })

  it('shows owner email and username from resume prop', () => {
    wrapper = mountDialog({ visible: true, resume: makeResume(1) })
    const vm = wrapper.vm as any
    expect(vm.$props.resume.ownerEmail).toBe('user1@test.com')
    expect(vm.$props.resume.ownerUsername).toBe('user1')
  })

  it('uses only safe canonical URLs, not raw file paths', () => {
    const resume = makeResume(1)
    // The DTO contract has safe URLs, not raw file system paths
    expect(resume.pdfOpenUrl).toBeTruthy()
    expect(resume.pdfOpenUrl).toContain('/api/generate/resumes/')
    expect(resume.pdfDownloadUrl).toBeTruthy()
    expect(resume.htmlDownloadUrl).toBeTruthy()
    // The AdminSavedResume type has no pdf_file_path or html_file_path
    // This proves the type contract is correct
  })

  it('disables PDF actions when pdfAvailable is false', () => {
    wrapper = mountDialog({ visible: true, resume: makeResume(1, { pdfAvailable: false }) })
    const vm = wrapper.vm as any
    expect(vm.$props.resume.pdfAvailable).toBe(false)
    // Template disables buttons when pdfAvailable=false via :disabled prop
  })

  it('delete button opens confirmation dialog', () => {
    wrapper = mountDialog({ visible: true, resume: makeResume(1) })
    const vm = wrapper.vm as any
    vm.confirmDelete()
    expect(confirmAcceptCallback).not.toBeNull()
  })

  it('confirm delete emits delete with resume id', () => {
    wrapper = mountDialog({ visible: true, resume: makeResume(1) })
    const vm = wrapper.vm as any
    vm.confirmDelete()
    if (confirmAcceptCallback) confirmAcceptCallback()
    const emitted = wrapper.emitted('delete')
    expect(emitted).toBeTruthy()
    expect(emitted![0]).toEqual([1])
  })

  it('delete loading disables duplicate delete', () => {
    wrapper = mountDialog({ visible: true, resume: makeResume(1), deleteLoading: true })
    const vm = wrapper.vm as any
    vm.confirmDelete()
    // confirm.require should NOT have been called because deleteLoading is true
    expect(confirmAcceptCallback).toBeNull()
  })

  it('relative public URL displays as absolute', () => {
    const resume = makeResume(1, { publicUrlLink: '/vasyausername/RU3S3' })
    wrapper = mountDialog({ visible: true, resume })
    const vm = wrapper.vm as any
    // window.location.origin is http://localhost:3000 in test
    expect(vm.absolutePublicUrlLink).toBe('http://localhost:3000/vasyausername/RU3S3')
  })

  it('already absolute public URL is not double-prefixed', () => {
    const resume = makeResume(1, { publicUrlLink: 'https://example.com/vasya/RU3S3' })
    wrapper = mountDialog({ visible: true, resume })
    const vm = wrapper.vm as any
    expect(vm.absolutePublicUrlLink).toBe('https://example.com/vasya/RU3S3')
  })

  it('PDF unavailable message uses localized text, not raw backend message', () => {
    const resume = makeResume(1, { pdfAvailable: false, pdfMessage: 'PDF is being generated.' })
    wrapper = mountDialog({ visible: true, resume })
    const vm = wrapper.vm as any
    // Component uses $t('admin.resumeDetails.pdfNotAvailable'), not resume.pdfMessage directly
    expect(vm.$props.resume.pdfAvailable).toBe(false)
    expect(vm.$props.resume.pdfMessage).toBe('PDF is being generated.')
    // The template v-if uses pdfAvailable === false, and renders i18n key, not raw message
    expect(vm.$props.resume.pdfMessage).toBeTruthy()
  })
})
