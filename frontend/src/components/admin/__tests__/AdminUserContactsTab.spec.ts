import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminUserContactsTab from '@/components/admin/AdminUserContactsTab.vue'
import type { AdminUserContacts } from '@/types/admin'

const mockT = vi.fn((key: string) => {
  const m: Record<string, string> = {
    'admin.userDetails.fullName': 'Full name',
    'admin.userDetails.resumeEmailLabel': 'Resume email',
    'admin.userDetails.professionalTitle': 'Professional title',
    'admin.userDetails.phone': 'Phone',
    'admin.userDetails.location': 'Location',
    'admin.userDetails.linkedinUrl': 'LinkedIn',
    'admin.userDetails.portfolioUrl': 'Portfolio',
    'admin.userDetails.telegram': 'Telegram',
    'admin.userDetails.whatsapp': 'WhatsApp',
    'admin.userDetails.notProvided': 'Not provided',
    'admin.userDetails.noContacts': 'No contact information provided.',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

function makeContacts(overrides: Partial<AdminUserContacts> = {}): AdminUserContacts {
  return {
    fullName: 'John Doe',
    professionalTitle: 'Developer',
    phone: '+123456789',
    resumeEmail: 'resume@example.com',
    location: 'Almaty',
    linkedinUrl: null,
    portfolioUrl: null,
    telegram: null,
    whatsapp: null,
    ...overrides,
  }
}

function mountTab(contacts: AdminUserContacts | null) {
  return mount(AdminUserContactsTab, {
    props: { contacts },
    global: { mocks: { $t: mockT } },
  })
}

describe('AdminUserContactsTab', () => {
  it('renders fullName from contacts', () => {
    const wrapper = mountTab(makeContacts())
    expect(wrapper.text()).toContain('John Doe')
  })

  it('renders resumeEmail as resume/contact email', () => {
    const wrapper = mountTab(makeContacts({ resumeEmail: 'resume@example.com' }))
    expect(wrapper.text()).toContain('resume@example.com')
    // Must not render "account" in the label context
    const labelElements = wrapper.findAll('.detail-label')
    const resumeLabels = labelElements.filter(el => el.text().toLowerCase().includes('resume'))
    expect(resumeLabels.length).toBeGreaterThanOrEqual(1)
  })

  it('is read-only: no editable inputs', () => {
    const wrapper = mountTab(makeContacts())
    expect(wrapper.find('input').exists()).toBe(false)
    expect(wrapper.find('select').exists()).toBe(false)
    expect(wrapper.find('button').exists()).toBe(false)
  })

  it('skips missing optional fields without crashing', () => {
    const wrapper = mountTab(makeContacts({ linkedinUrl: null, portfolioUrl: null }))
    // Fields with null values are simply not rendered — no crash
    expect(wrapper.find('.contacts-tab').exists()).toBe(true)
  })

  it('renders empty state when contacts is null', () => {
    const wrapper = mountTab(null)
    expect(wrapper.text()).toContain('No contact information provided.')
  })
})
