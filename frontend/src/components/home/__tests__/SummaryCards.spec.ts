/**
 * SummaryCards tests.
 *
 * Tests that the latest-resume card is clickable only when lastResume exists.
 * Regression: old code would emit openLastResume even when lastResume was null.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import SummaryCards from '@/components/home/SummaryCards.vue'
import type { SavedResumeData } from '@/services/userHomeService'

// ── Mocks ───────────────────────────────────────────────────────────

const mockT = vi.fn((key: string) => {
  const translations: Record<string, string> = {
    'home.summary.savedResumes': 'Saved resumes',
    'home.summary.profileStatus': 'Profile status',
    'home.summary.ready': 'Ready',
    'home.summary.needsInfo': 'Needs info',
    'home.summary.readyHint': 'Go on with making strong resumes!',
    'home.summary.needsInfoHint': 'Add contact details, work experience, and education.',
    'home.summary.incompleteResumesHint': 'Add required info.',
    'home.summary.readyStatusHint': 'Profile is ready!',
    'home.summary.lastResume': 'Last resume',
    'home.summary.noLastResume': 'No resumes yet',
    'home.summary.updateProfile': 'Update profile',
    'home.summary.completeProfile': 'Complete profile',
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

function makeResume(overrides: Partial<SavedResumeData> = {}): SavedResumeData {
  return {
    id: 1,
    resumeTitle: 'Senior Developer - Acme',
    vacancyTitle: 'Senior Developer',
    companyName: 'Acme Corp',
    languageCode: 'EN',
    adaptationLevel: 'BALANCED',
    createdAt: '2026-06-20',
    publicUrlLink: 'http://localhost:8080/user/CODE',
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

function mountCards(props: {
  savedResumesCount?: number
  profileReady?: boolean
  lastResume: SavedResumeData | null
}) {
  return mount(SummaryCards, {
    props: {
      savedResumesCount: props.savedResumesCount ?? 0,
      profileReady: props.profileReady ?? false,
      lastResume: props.lastResume,
    },
    global: {
      mocks: { $t: mockT },
    },
  })
}

// ── Tests ───────────────────────────────────────────────────────────

describe('SummaryCards latest resume behavior', () => {
  // T048: latest card emits openLastResume only when lastResume exists
  it('when lastResume exists, clicking the card emits openLastResume', async () => {
    const wrapper = mountCards({ lastResume: makeResume() })

    // The last-resume card should have the clickable class
    const cards = wrapper.findAll('.summary-card')
    const lastResumeCards = cards.filter(c => c.text().includes('Senior Developer - Acme'))
    expect(lastResumeCards.length).toBeGreaterThanOrEqual(1)

    await lastResumeCards[0].trigger('click')

    const emitted = wrapper.emitted('openLastResume')
    expect(emitted).toBeTruthy()
    expect(emitted).toHaveLength(1)
  })

  // T048: latest card is NOT clickable when lastResume is null
  it('when lastResume is null, clicking the card does NOT emit openLastResume', async () => {
    const wrapper = mountCards({ lastResume: null })

    // Find the third card (last resume card)
    const cards = wrapper.findAll('.summary-card')

    // The last card should NOT have clickable class (no cursor:pointer)
    const clickableCards = cards.filter(c => c.classes().includes('clickable'))
    expect(clickableCards.length).toBe(0)

    // Click every card to ensure no emission
    for (const card of cards) {
      await card.trigger('click')
    }

    const emitted = wrapper.emitted('openLastResume')
    expect(emitted).toBeFalsy()
  })

  // T048: card shows "No resumes yet" when lastResume is null
  it('shows no-last-resume text when lastResume is null', () => {
    const wrapper = mountCards({ lastResume: null })
    expect(wrapper.text()).toContain('No resumes yet')
  })

  // T048: card shows resume title when lastResume exists
  it('shows resume title when lastResume exists', () => {
    const wrapper = mountCards({ lastResume: makeResume() })
    expect(wrapper.text()).toContain('Senior Developer - Acme')
    expect(wrapper.text()).toContain('2026-06-20')
  })
})
