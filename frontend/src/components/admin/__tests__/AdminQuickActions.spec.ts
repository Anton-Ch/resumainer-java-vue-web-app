import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminQuickActions from '@/components/admin/AdminQuickActions.vue'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (k: string) => k,
    locale: ref('en'),
  }),
}))

function mountActions() {
  return mount(AdminQuickActions, {
    global: {
      mocks: { $t: (k: string) => k },
    },
  })
}

describe('AdminQuickActions', () => {
  beforeEach(() => {
    mockPush.mockClear()
  })

  it('Users action navigates to /admin/users', async () => {
    const wrapper = mountActions()
    const buttons = wrapper.findAll('.nav-card')
    const usersBtn = buttons.find(b => b.text().includes('admin.quickActions.users'))
    expect(usersBtn).toBeTruthy()
    await usersBtn!.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/admin/users')
  })

  it('Resumes action emits scrollToResumes and does NOT navigate', async () => {
    const wrapper = mountActions()
    const buttons = wrapper.findAll('.nav-card')
    const resumesBtn = buttons.find(b => b.text().includes('admin.quickActions.resumes'))
    expect(resumesBtn).toBeTruthy()
    await resumesBtn!.trigger('click')
    // Must NOT push /admin/resumes
    expect(mockPush).not.toHaveBeenCalledWith(expect.stringContaining('/admin/resumes'))
    // Must emit scroll event
    expect(wrapper.emitted('scrollToResumes')).toBeTruthy()
  })

  it('AI Models action navigates to /admin/ai-models', async () => {
    const wrapper = mountActions()
    const buttons = wrapper.findAll('.nav-card')
    const aiBtn = buttons.find(b => b.text().includes('admin.quickActions.aiModels'))
    expect(aiBtn).toBeTruthy()
    await aiBtn!.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/admin/ai-models')
  })

  it('renders three nav cards', () => {
    const wrapper = mountActions()
    expect(wrapper.findAll('.nav-card').length).toBe(3)
  })
})
