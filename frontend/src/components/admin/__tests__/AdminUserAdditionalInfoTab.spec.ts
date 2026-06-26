import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminUserAdditionalInfoTab from '@/components/admin/AdminUserAdditionalInfoTab.vue'
import type { AdminUserAdditionalInfo } from '@/types/admin'

const mockT = vi.fn((key: string) => {
  const m: Record<string, string> = {
    'admin.userDetails.skills': 'Skills',
    'admin.userDetails.spokenLanguages': 'Languages',
    'admin.userDetails.professionalAspirations': 'Aspirations',
    'admin.userDetails.achievements': 'Achievements',
    'admin.userDetails.generalInformation': 'General',
    'admin.userDetails.readyForRelocation': 'Relocation',
    'admin.userDetails.readyForBusinessTrips': 'Business trips',
    'admin.userDetails.dateOfBirth': 'Date of birth',
    'admin.userDetails.citizenship': 'Citizenship',
    'admin.userDetails.noAdditionalInfo': 'No additional info.',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

function makeInfo(overrides: Partial<AdminUserAdditionalInfo> = {}): AdminUserAdditionalInfo {
  return {
    skills: 'Java, Spring',
    languages: 'English, Russian',
    professionalAspirations: 'Grow',
    achievements: null,
    generalInformation: null,
    readyForRelocation: 'YES',
    readyForBusinessTrips: 'NO',
    dateOfBirth: '1993-01-01',
    citizenship: 'Kazakhstan',
    ...overrides,
  }
}

function mountTab(info: AdminUserAdditionalInfo | null) {
  return mount(AdminUserAdditionalInfoTab, {
    props: { info },
    global: { mocks: { $t: mockT } },
  })
}

describe('AdminUserAdditionalInfoTab', () => {
  it('renders skills and languages when present', () => {
    const wrapper = mountTab(makeInfo())
    expect(wrapper.text()).toContain('Java, Spring')
    expect(wrapper.text()).toContain('English, Russian')
  })

  it('renders citizenship and date of birth', () => {
    const wrapper = mountTab(makeInfo())
    expect(wrapper.text()).toContain('1993-01-01')
    expect(wrapper.text()).toContain('Kazakhstan')
  })

  it('skips null fields without crashing', () => {
    const wrapper = mountTab(makeInfo({ achievements: null, generalInformation: null }))
    expect(wrapper.text()).not.toContain('Achievements')
  })

  it('is read-only: no editable inputs', () => {
    const wrapper = mountTab(makeInfo())
    expect(wrapper.find('input').exists()).toBe(false)
    expect(wrapper.find('select').exists()).toBe(false)
    expect(wrapper.find('button').exists()).toBe(false)
  })

  it('renders empty state when info is null', () => {
    const wrapper = mountTab(null)
    expect(wrapper.text()).toContain('No additional info.')
  })
})
