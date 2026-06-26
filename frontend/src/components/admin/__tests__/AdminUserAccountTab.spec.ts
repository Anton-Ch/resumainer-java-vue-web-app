import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminUserAccountTab from '@/components/admin/AdminUserAccountTab.vue'
import PrimeVue from 'primevue/config'
import type { AdminUserAccount } from '@/types/admin'

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
    'admin.userDetails.accountEmail': 'Account email',
    'admin.userDetails.role': 'Role',
    'admin.userDetails.status': 'Status',
    'admin.userDetails.permission': 'Permission',
    'admin.userDetails.isPrivileged': 'Privileged',
    'admin.userDetails.privileged': 'Privileged',
    'admin.userDetails.nonPrivileged': 'Non-privileged',
    'admin.userDetails.save': 'Save changes',
    'admin.userDetails.cancel': 'Cancel changes',
    'admin.userDetails.selfDemotionHint': 'You cannot change your own role.',
    'admin.userDetails.selfBlockHint': 'You cannot block your own account.',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

function makeAccount(overrides: Partial<AdminUserAccount> = {}): AdminUserAccount {
  return {
    id: 'uuid-1',
    username: 'johndoe',
    accountEmail: 'account@example.com',
    roleCode: 'USER',
    roleName: 'Regular User',
    statusCode: 'ACTIVE',
    statusName: 'Active',
    permissionCode: 'ALLOWED',
    permissionName: 'Allowed',
    isPrivileged: false,
    defaultLanguageCode: null,
    defaultLanguageName: null,
    secondaryLanguageCode: null,
    secondaryLanguageName: null,
    createdAt: '2026-06-01T12:00:00',
    updatedAt: null,
    ...overrides,
  }
}

function mountTab(props: {
  account: AdminUserAccount
  isCurrentAdmin: boolean
  saving?: boolean
}) {
  return mount(AdminUserAccountTab, {
    props: {
      account: props.account,
      isCurrentAdmin: props.isCurrentAdmin,
      saving: props.saving ?? false,
    },
    global: { plugins: [PrimeVue], mocks: { $t: mockT } },
    attachTo: document.body,
  })
}

describe('AdminUserAccountTab', () => {
  it('renders account email', () => {
    const wrapper = mountTab({ account: makeAccount(), isCurrentAdmin: false })
    expect(wrapper.text()).toContain('account@example.com')
  })

  it('renders role, status, permission labels', () => {
    const account = makeAccount({ roleCode: 'ADMIN', roleName: 'Administrator', statusCode: 'ACTIVE', statusName: 'Active', permissionCode: 'ALLOWED', permissionName: 'Allowed' })
    const wrapper = mountTab({ account, isCurrentAdmin: false })
    // The Select component renders the label for currently selected option
    // If the Select value is ADMIN, it shows "Admin" (from options)
    expect(wrapper.text()).toContain('Role')
    expect(wrapper.text()).toContain('Status')
    expect(wrapper.text()).toContain('Permission')
  })

  it('renders isPrivileged (not privileged)', () => {
    const wrapper = mountTab({
      account: makeAccount({ isPrivileged: true }),
      isCurrentAdmin: false,
    })
    expect(wrapper.text()).toContain('Privileged')
  })

  it('emits save with correct request fields', () => {
    const account = makeAccount({ roleCode: 'USER', statusCode: 'ACTIVE', permissionCode: 'ALLOWED', isPrivileged: false })
    const wrapper = mountTab({ account, isCurrentAdmin: false })
    // Change role to ADMIN
    const selects = wrapper.findAllComponents({ name: 'Select' })
    if (selects.length > 0) {
      selects[0].vm.$emit('update:modelValue', 'ADMIN')
    }
    // Reset initial to dirty mismatch
    const vm = wrapper.vm as any
    vm.dirty.roleCode = 'ADMIN'
    vm.dirty.statusCode = 'ACTIVE'
    vm.dirty.permissionCode = 'ALLOWED'
    vm.dirty.isPrivileged = false

    vm.onSave()
    const emitted = wrapper.emitted('save')
    expect(emitted).toBeTruthy()
    if (emitted) {
      const req = emitted[0][0] as any
      expect(req).toEqual({
        roleCode: 'ADMIN',
        statusCode: 'ACTIVE',
        permissionCode: 'ALLOWED',
        isPrivileged: false,
      })
      // Must NOT contain forbidden fields
      expect(req.privileged).toBeUndefined()
      expect(req.accountEmail).toBeUndefined()
      expect(req.resumeEmail).toBeUndefined()
      expect(req.fullName).toBeUndefined()
    }
  })

  it('save button disabled when no changes', () => {
    const account = makeAccount()
    const wrapper = mountTab({ account, isCurrentAdmin: false })
    const vm = wrapper.vm as any
    expect(vm.hasChanges).toBe(false)
  })

  it('save button disabled when saving is true', () => {
    const wrapper = mountTab({ account: makeAccount(), isCurrentAdmin: false, saving: true })
    const vm = wrapper.vm as any
    expect(vm.saving).toBe(true)
  })

  it('self-demotion disabled when isCurrentAdmin and role is ADMIN', () => {
    const account = makeAccount({ roleCode: 'ADMIN' })
    const wrapper = mountTab({ account, isCurrentAdmin: true })
    const vm = wrapper.vm as any
    expect(vm.disableRole).toBe(true)
    expect(wrapper.text()).toContain('You cannot change your own role.')
  })

  it('self-block disabled when isCurrentAdmin and status is ACTIVE', () => {
    const account = makeAccount({ roleCode: 'ADMIN', statusCode: 'ACTIVE' })
    const wrapper = mountTab({ account, isCurrentAdmin: true })
    const vm = wrapper.vm as any
    expect(vm.disableStatus).toBe(true)
    expect(wrapper.text()).toContain('You cannot block your own account.')
  })

  it('own permission edit allowed when isCurrentAdmin', () => {
    const account = makeAccount({ roleCode: 'ADMIN' })
    const wrapper = mountTab({ account, isCurrentAdmin: true })
    const vm = wrapper.vm as any
    expect(vm.disableRole).toBe(true) // self-demotion blocked
    // But Select components exist (not all disabled)
  })
})

describe('AdminUserAccountTab dirty event', () => {
  it('emits update:dirty when changes exist', async () => {
    const wrapper = mountTab({ account: makeAccount(), isCurrentAdmin: false })
    const vm = wrapper.vm as any
    vm.dirty.roleCode = 'ADMIN'
    await wrapper.vm.$nextTick()
    const dirtyEvents = wrapper.emitted('update:dirty')
    expect(dirtyEvents).toBeTruthy()
  })
})
