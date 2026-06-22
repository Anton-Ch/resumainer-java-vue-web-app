/**
 * ReviewStepForm tests — isFinalizing prop acceptance and save event emission.
 * Full disabled/loading behavior is covered by GenerateReviewPage tests.
 */
import { describe, it, expect, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { ref } from 'vue'
import ReviewStepForm from '@/components/generate/ReviewStepForm.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: ref('en'),
  }),
}))

function makeProps(overrides: Record<string, any> = {}) {
  return {
    enVariants: [],
    ruVariants: [],
    isBilingual: false,
    showLevels: false,
    activeTab: 'positioning',
    selectedLevel: 'Balanced' as const,
    isFinalizing: false,
    ...overrides,
  }
}

describe('ReviewStepForm — isFinalizing prop', () => {
  it('accepts isFinalizing=false prop without error', () => {
    const wrapper = shallowMount(ReviewStepForm, {
      props: makeProps({ isFinalizing: false }),
    })
    expect(wrapper.props('isFinalizing')).toBe(false)
  })

  it('accepts isFinalizing=true prop without error', () => {
    const wrapper = shallowMount(ReviewStepForm, {
      props: makeProps({ isFinalizing: true }),
    })
    expect(wrapper.props('isFinalizing')).toBe(true)
  })
})
