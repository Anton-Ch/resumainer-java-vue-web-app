import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminStatsCards from '@/components/admin/AdminStatsCards.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        'admin.stats.totalUsers': 'Total users',
        'admin.stats.totalResumes': 'Total resumes',
        'admin.stats.totalTokensSent': 'Total tokens sent',
        'admin.stats.totalTokensGenerated': 'Total tokens generated',
        'admin.stats.wip': 'WIP',
      }
      return m[key] || key
    },
    locale: ref('en'),
  }),
}))

function mountCards(props = {}) {
  return mount(AdminStatsCards, {
    props: {
      totalUsers: 0,
      totalResumes: 0,
      totalTokensSent: 0,
      totalTokensSentWip: false,
      totalTokensGenerated: 0,
      totalTokensGeneratedWip: false,
      ...props,
    },
    global: { mocks: { $t: (k: string) => k } },
  })
}

describe('AdminStatsCards', () => {
  it('renders total users from props', () => {
    const wrapper = mountCards({ totalUsers: 15 })
    expect(wrapper.text()).toContain('15')
  })

  it('renders total resumes from props', () => {
    const wrapper = mountCards({ totalResumes: 42 })
    expect(wrapper.text()).toContain('42')
  })

  it('renders token sent value as 0', () => {
    const wrapper = mountCards({ totalTokensSent: 0, totalTokensSentWip: true })
    expect(wrapper.text()).toContain('0')
  })

  it('renders token generated value as 0', () => {
    const wrapper = mountCards({ totalTokensGenerated: 0, totalTokensGeneratedWip: true })
    expect(wrapper.text()).toContain('0')
  })

  it('shows WIP badge when totalTokensSentWip is true', () => {
    const wrapper = mountCards({ totalTokensSent: 0, totalTokensSentWip: true })
    const badges = wrapper.findAll('.wip-badge')
    expect(badges.length).toBeGreaterThanOrEqual(1)
  })

  it('shows WIP badge when totalTokensGeneratedWip is true', () => {
    const wrapper = mountCards({ totalTokensGenerated: 0, totalTokensGeneratedWip: true })
    const badges = wrapper.findAll('.wip-badge')
    expect(badges.length).toBeGreaterThanOrEqual(1)
  })

  it('hides WIP badge when flags are false', () => {
    const wrapper = mountCards({ totalTokensSentWip: false, totalTokensGeneratedWip: false })
    expect(wrapper.find('.wip-badge').exists()).toBe(false)
  })

  it('renders four stat cards', () => {
    const wrapper = mountCards()
    const cards = wrapper.findAll('.stat-card')
    expect(cards.length).toBe(4)
  })
})
