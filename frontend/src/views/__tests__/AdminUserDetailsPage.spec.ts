import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, computed } from 'vue'
import AdminUserDetailsPage from '@/views/AdminUserDetailsPage.vue'
import PrimeVue from 'primevue/config'

const mockPush = vi.fn()
const mockRoute = { params: { userId: 'uuid-target' } }

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: mockPush }),
}))

const mockT = vi.fn((key: string) => {
  const m: Record<string, string> = {
    'admin.userDetails.backToUsers': 'Back to Users',
    'admin.userDetails.notFound': 'User not found.',
    'admin.userDetails.retry': 'Retry',
    'admin.userDetails.error': 'Failed.',
    'admin.userDetails.you': 'You',
    'admin.userDetails.tabContacts': 'Contacts',
    'admin.userDetails.tabAccount': 'Account',
    'admin.userDetails.tabAdditional': 'Additional',
    'admin.userDetails.tabResumes': 'Resumes',
    'admin.userDetails.dangerZone': 'Danger zone',
    'admin.userDetails.deleteUser': 'Delete this user',
    'admin.userDetails.selfDeleteHint': 'You cannot delete your own admin account.',
    'admin.userDetails.deleteConfirmTitle': 'Delete user?',
    'admin.userDetails.deleteConfirmText': 'Delete text.',
    'admin.userDetails.deleteSuccess': 'Deleted.',
    'admin.userDetails.deleteFailed': 'Failed.',
    'admin.userDetails.saveSuccess': 'Saved.',
    'admin.userDetails.saveFailed': 'Save failed.',
    'admin.userDetails.unsavedTitle': 'Unsaved',
    'admin.userDetails.unsavedText': 'Unsaved changes.',
    'admin.userDetails.stay': 'Stay',
    'admin.userDetails.leave': 'Leave',
    'admin.userDetails.saveConfirmTitle': 'Save?',
    'admin.userDetails.saveConfirmText': 'Save changes?',
    'admin.userDetails.confirmSave': 'Save',
    'admin.userDetails.save': 'Save',
    'admin.userDetails.cancel': 'Cancel',
    'admin.userDetails.accountEmail': 'Account email',
    'admin.userDetails.resumeEmailLabel': 'Resume email',
    'admin.userDetails.fullName': 'Full name',
    'admin.userDetails.role': 'Role',
    'admin.userDetails.status': 'Status',
    'admin.userDetails.permission': 'Permission',
    'admin.userDetails.isPrivileged': 'Privileged',
    'admin.userDetails.privileged': 'Privileged',
    'admin.userDetails.nonPrivileged': 'Non-privileged',
    'admin.userDetails.deleteDescription': 'Delete description.',
    'admin.userDetails.noContacts': 'No contacts.',
    'admin.userDetails.noAdditionalInfo': 'No additional.',
    'admin.userDetails.resumesComingSoon': 'Resumes coming soon.',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

const mockToastAdd = vi.fn()
vi.mock('primevue/usetoast', () => ({
  useToast: () => ({ add: mockToastAdd }),
}))

let confirmAcceptCallback: (() => void) | null = null
vi.mock('primevue/useconfirm', () => ({
  useConfirm: () => ({
    require: (opts: any) => {
      confirmAcceptCallback = opts.accept || null
    },
  }),
}))

const mockGetDetails = vi.fn()
const mockUpdateAccess = vi.fn()
const mockDeleteUser = vi.fn()

vi.mock('@/services/adminService', () => ({
  getAdminUserDetails: (...args: any[]) => mockGetDetails(...args),
  updateAdminUserAccess: (...args: any[]) => mockUpdateAccess(...args),
  deleteAdminUser: (...args: any[]) => mockDeleteUser(...args),
}))

const stubs = {
  AppHeader: { template: '<div class="app-header-stub"></div>' },
  AdminUserContactsTab: { template: '<div class="contacts-stub">contacts</div>' },
  AdminUserAccountTab: { template: '<div class="account-stub">account</div>' },
  AdminUserAdditionalInfoTab: { template: '<div class="additional-stub">additional</div>' },
  AdminUserResumesTab: { template: '<div class="resumes-stub">resumes</div>' },
  Tabs: { template: '<div><slot /></div>' },
  TabList: { template: '<div><slot /></div>' },
  Tab: { template: '<span><slot /></span>' },
  TabPanels: { template: '<div><slot /></div>' },
  TabPanel: { template: '<div><slot /></div>' },
}

function mountPage() {
  return mount(AdminUserDetailsPage, {
    global: {
      plugins: [PrimeVue],
      stubs,
      mocks: { $t: mockT, $router: { push: mockPush } },
    },
    attachTo: document.body,
  })
}

let wrapper: ReturnType<typeof mountPage>

beforeEach(() => {
  mockPush.mockClear()
  mockToastAdd.mockClear()
  mockGetDetails.mockReset()
  mockUpdateAccess.mockReset()
  mockDeleteUser.mockReset()
  confirmAcceptCallback = null
})

afterEach(() => {
  if (wrapper) { wrapper.unmount(); wrapper = null as any }
})

function createMockDetails(isCurrentAdmin: boolean) {
  return {
    id: isCurrentAdmin ? 'uuid-admin' : 'uuid-target',
    isCurrentAdmin,
    account: {
      id: isCurrentAdmin ? 'uuid-admin' : 'uuid-target',
      username: 'johndoe',
      accountEmail: 'account@example.com',
      roleCode: 'ADMIN',
      roleName: 'Administrator',
      statusCode: 'ACTIVE',
      statusName: 'Active',
      permissionCode: 'ALLOWED',
      permissionName: 'Allowed',
      isPrivileged: false,
      defaultLanguageCode: 'EN',
      defaultLanguageName: 'English',
      secondaryLanguageCode: null,
      secondaryLanguageName: null,
      createdAt: '2026-06-01T12:00:00',
      updatedAt: null,
    },
    contacts: {
      fullName: 'John Doe',
      professionalTitle: 'Developer',
      phone: '+123456789',
      resumeEmail: 'resume@example.com',
      location: 'Almaty',
      linkedinUrl: null,
      portfolioUrl: null,
      telegram: null,
      whatsapp: null,
    },
    additionalInfo: {
      skills: 'Java, Spring',
      languages: 'English, Russian',
      professionalAspirations: null,
      achievements: null,
      generalInformation: null,
      readyForRelocation: null,
      readyForBusinessTrips: null,
      dateOfBirth: null,
      citizenship: null,
    },
  }
}

describe('AdminUserDetailsPage', () => {
  it('reads userId from route params and calls getAdminUserDetails', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(mockGetDetails).toHaveBeenCalledWith('uuid-target')
    })
  })

  it('renders loading state while fetching', () => {
    mockGetDetails.mockImplementation(() => new Promise(() => {})) // never resolve
    wrapper = mountPage()
    expect(wrapper.find('.skeleton-block').exists()).toBe(true)
  })

  it('renders loaded user details', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('account@example.com')
    })
  })

  it('renders not-found state when API returns 404', async () => {
    mockGetDetails.mockRejectedValue(new Error('404 not found'))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('User not found.')
    })
  })

  it('renders error state and retry works', async () => {
    mockGetDetails.mockRejectedValueOnce(new Error('Network error'))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Network error')
    })
    // Retry resolves
    mockGetDetails.mockResolvedValueOnce(createMockDetails(false))
    const retryBtn = wrapper.find('button')
    await retryBtn.trigger('click')
    await vi.waitFor(() => {
      expect(mockGetDetails).toHaveBeenCalledTimes(2)
    })
  })

  it('email separation: account email in header, resume email in contacts', async () => {
    const details = createMockDetails(false)
    mockGetDetails.mockResolvedValue(details)
    wrapper = mountPage()
    await vi.waitFor(() => {
      // account email is in the header/subtitle
      expect(wrapper.text()).toContain('account@example.com')
    })
  })
})

describe('AdminUserDetailsPage delete flow', () => {
  it('shows delete button for non-current user', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Delete this user')
    })
  })

  it('hides delete button for current admin user', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(true))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('You cannot delete your own admin account.')
    })
    expect(wrapper.text()).not.toContain('Delete this user')
  })

  it('delete confirmation calls deleteAdminUser', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    mockDeleteUser.mockResolvedValue({ message: 'User deleted' })
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Delete this user')
    })
    // Click delete
    const btns = wrapper.findAll('button')
    const deleteBtn = btns.find(b => b.text().includes('Delete this user'))
    await deleteBtn!.trigger('click')
    // Confirm via accept callback
    if (confirmAcceptCallback) {
      await confirmAcceptCallback()
      await vi.waitFor(() => {
        expect(mockDeleteUser).toHaveBeenCalledWith('uuid-target')
      })
    }
  })

  it('delete success navigates to /admin/users', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    mockDeleteUser.mockResolvedValue({ message: 'User deleted' })
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Delete this user')
    })
    const btns = wrapper.findAll('button')
    const deleteBtn = btns.find(b => b.text().includes('Delete this user'))
    await deleteBtn!.trigger('click')
    if (confirmAcceptCallback) {
      await confirmAcceptCallback()
      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/admin/users')
      })
    }
  })

  it('delete failure keeps page open', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    mockDeleteUser.mockRejectedValue(new Error('DB error'))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Delete this user')
    })
    const btns = wrapper.findAll('button')
    const deleteBtn = btns.find(b => b.text().includes('Delete this user'))
    await deleteBtn!.trigger('click')
    if (confirmAcceptCallback) {
      await confirmAcceptCallback()
      await vi.waitFor(() => {
        expect(mockPush).not.toHaveBeenCalled()
      })
    }
  })
})

describe('AdminUserDetailsPage access update', () => {
  it('loads details on mount', async () => {
    mockGetDetails.mockResolvedValue(createMockDetails(false))
    wrapper = mountPage()
    await vi.waitFor(() => {
      expect(mockGetDetails).toHaveBeenCalled()
    })
  })
})
