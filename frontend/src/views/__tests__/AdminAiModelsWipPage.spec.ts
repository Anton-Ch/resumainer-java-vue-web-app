import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminAiModelsWipPage from '@/views/AdminAiModelsWipPage.vue'
import PrimeVue from 'primevue/config'

const mockT = vi.fn((key: string) => {
  const m: Record<string, string> = {
    'admin.aiModels.title': 'AI Models',
    'admin.aiModels.wipBadge': 'Work in progress',
    'admin.aiModels.description': 'Description text.',
    'admin.aiModels.safetyNote': 'No API keys displayed here.',
    'admin.aiModels.plannedTitle': 'Planned',
    'admin.aiModels.planModelList': 'Model list',
    'admin.aiModels.planProviderConfig': 'Provider config',
    'admin.aiModels.planDefaultModel': 'Default model',
    'admin.aiModels.planUsageCost': 'Usage cost',
    'admin.aiModels.backToHome': 'Back to Admin Home',
    'admin.aiModels.goToUsers': 'Go to Users',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

const stubs = {
  AppHeader: { template: '<div class="app-header-stub"></div>' },
  RouterLink: { template: '<a class="router-link-stub"><slot /></a>' },
}

function mountPage() {
  return mount(AdminAiModelsWipPage, {
    global: {
      plugins: [PrimeVue],
      stubs,
      mocks: { $t: mockT },
    },
  })
}

describe('AdminAiModelsWipPage', () => {
  it('renders title', () => {
    const wrapper = mountPage()
    expect(wrapper.text()).toContain('AI Models')
  })

  it('renders WIP badge', () => {
    const wrapper = mountPage()
    expect(wrapper.text()).toContain('Work in progress')
  })

  it('renders safety note about no API keys', () => {
    const wrapper = mountPage()
    expect(wrapper.text()).toContain('No API keys displayed here.')
  })

  it('renders navigation link to /admin', () => {
    const wrapper = mountPage()
    expect(wrapper.text()).toContain('Back to Admin Home')
  })

  it('renders navigation link to /admin/users', () => {
    const wrapper = mountPage()
    expect(wrapper.text()).toContain('Go to Users')
  })

  it('does not render CRUD buttons', () => {
    const wrapper = mountPage()
    const text = wrapper.text()
    expect(text).not.toContain('Create')
    expect(text).not.toContain('Save')
    expect(text).not.toContain('Delete')
    expect(text).not.toContain('Edit')
  })

  it('does not render API key input', () => {
    const wrapper = mountPage()
    expect(wrapper.find('input[type="password"]').exists()).toBe(false)
    expect(wrapper.find('input[type="text"]').exists()).toBe(false)
  })

  it('renders planned features list', () => {
    const wrapper = mountPage()
    expect(wrapper.text()).toContain('Planned')
    expect(wrapper.text()).toContain('Model list')
    expect(wrapper.text()).toContain('Provider config')
  })
})
