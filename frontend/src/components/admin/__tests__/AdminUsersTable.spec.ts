import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AdminUsersTable from '@/components/admin/AdminUsersTable.vue'
import PrimeVue from 'primevue/config'
import type { AdminUserListItem } from '@/types/admin'

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
    'admin.usersTable.searchPlaceholder': 'Search',
    'admin.usersTable.clear': 'Clear',
    'admin.usersTable.clearTooltip': 'Clear filters',
    'admin.usersTable.all': 'All',
    'admin.usersTable.fullName': 'Full name',
    'admin.usersTable.username': 'Username',
    'admin.usersTable.email': 'Email',
    'admin.usersTable.role': 'Role',
    'admin.usersTable.status': 'Status',
    'admin.usersTable.permission': 'Permission',
    'admin.usersTable.rights': 'Rights',
    'admin.usersTable.privileged': 'Privileged',
    'admin.usersTable.nonPrivileged': 'Non-privileged',
    'admin.usersTable.resumesCount': 'Resumes',
    'admin.usersTable.created': 'Created',
    'admin.usersTable.dateFrom': 'From',
    'admin.usersTable.dateTo': 'To',
    'admin.usersTable.dateError': 'End date must be after start date.',
    'admin.usersTable.pageReport': 'Showing {first} to {last} of {totalRecords}',
    'admin.usersTable.noResults': 'No results',
    'admin.usersTable.emptyTitle': 'No users',
    'admin.usersTable.emptyText': 'No users found.',
    'admin.usersTable.retry': 'Retry',
    'admin.usersTable.error': 'Error.',
  }
  return m[key] || key
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: mockT, locale: ref('en') }),
}))

function makeUser(id: number, overrides: Partial<AdminUserListItem> = {}): AdminUserListItem {
  return {
    id: 'uuid-' + id,
    fullName: 'User ' + id,
    username: 'user' + id,
    email: 'user' + id + '@test.com',
    roleCode: id === 1 ? 'ADMIN' : 'USER',
    roleName: id === 1 ? 'Administrator' : 'Regular User',
    statusCode: 'ACTIVE',
    statusName: 'Active',
    permissionCode: 'ALLOWED',
    permissionName: 'Allowed',
    isPrivileged: id === 1,
    resumesCount: id * 2,
    createdAt: '2026-06-0' + id,
    ...overrides,
  }
}

function mountTable(props: {
  items: AdminUserListItem[]
  totalRecords?: number
  loading?: boolean
  first?: number
  sortField?: string
  sortOrder?: number
  size?: number
}) {
  return mount(AdminUsersTable, {
    props: {
      items: props.items,
      totalRecords: props.totalRecords ?? props.items.length,
      loading: props.loading ?? false,
      first: props.first ?? 0,
      sortField: props.sortField ?? 'createdAt',
      sortOrder: props.sortOrder ?? -1,
      size: props.size ?? 10,
    },
    global: { plugins: [PrimeVue], mocks: { $t: mockT } },
    attachTo: document.body,
  })
}

describe('AdminUsersTable', () => {
  it('renders rows from items prop', () => {
    const items = [makeUser(1)]
    const wrapper = mountTable({ items })
    expect(wrapper.text()).toContain('User 1')
    expect(wrapper.text()).toContain('user1')
  })

  it('renders email, role, status, permission, resumesCount', () => {
    const items = [makeUser(1)]
    const wrapper = mountTable({ items })
    expect(wrapper.text()).toContain('user1@test.com')
    expect(wrapper.text()).toContain('Administrator')
    expect(wrapper.text()).toContain('Active')
    expect(wrapper.text()).toContain('Allowed')
    expect(wrapper.text()).toContain('2')
  })

  it('renders isPrivileged (not privileged) for privileged user', () => {
    const items = [makeUser(1, { isPrivileged: true })]
    const wrapper = mountTable({ items })
    expect(wrapper.text()).toContain('Privileged')
  })

  it('renders isPrivileged as non-privileged for regular user', () => {
    const items = [makeUser(2, { isPrivileged: false })]
    const wrapper = mountTable({ items })
    expect(wrapper.text()).toContain('Non-privileged')
  })

  it('emits navigate on row click', async () => {
    const items = [makeUser(1)]
    const wrapper = mountTable({ items })
    const rows = wrapper.findAll('tr')
    const dataRow = rows.find(r => r.html().includes('User 1'))
    expect(dataRow).toBeTruthy()
    await dataRow!.trigger('click')
    const emitted = wrapper.emitted('navigate')
    expect(emitted).toBeTruthy()
    if (emitted) {
      expect(emitted[0][0]).toBe('uuid-1')
    }
  })

  it('emits search event via vm.onSearchInput', async () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.onSearchInput({ target: { value: 'john' } } as any)
    await new Promise(r => setTimeout(r, 400))
    const emitted = wrapper.emitted('search')
    expect(emitted).toBeTruthy()
    if (emitted) {
      expect(emitted[emitted.length - 1][0]).toBe('john')
    }
  })

  it('emits page event', () => {
    const wrapper = mountTable({ items: [makeUser(1)], totalRecords: 50 })
    const vm = wrapper.vm as any
    vm.onPage({ first: 10, rows: 10 })
    const emitted = wrapper.emitted('page')
    expect(emitted).toBeTruthy()
  })
})

describe('AdminUsersTable date validation', () => {
  it('blocks request when dateTo < dateFrom', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.dateFrom = new Date('2026-06-10')
    vm.dateTo = new Date('2026-06-01')
    vm.onFiltersChange()
    expect(vm.dateError).toBeTruthy()
  })

  it('passes valid date range', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.dateFrom = new Date('2026-06-01')
    vm.dateTo = new Date('2026-06-10')
    vm.dateError = null
    vm.onFiltersChange()
    const emitted = wrapper.emitted('filter')
    expect(emitted).toBeTruthy()
    expect(vm.dateError).toBeNull()
  })
})

describe('AdminUsersTable sort whitelist', () => {
  it('uses generationPermission sort key for permission column', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    // Simulate PrimeVue DataTable @sort event shape: { sortField, sortOrder }
    vm.onSort({ sortField: 'generationPermission', sortOrder: 1 })
    const emitted = wrapper.emitted('sort')
    expect(emitted).toBeTruthy()
    if (emitted) {
      expect(emitted[emitted.length - 1][0]).toEqual({ field: 'generationPermission', order: 'asc' })
    }
  })

  it('uses rights sort key for privileged column', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.onSort({ sortField: 'rights', sortOrder: -1 })
    const emitted = wrapper.emitted('sort')
    expect(emitted).toBeTruthy()
    if (emitted) {
      expect(emitted[emitted.length - 1][0]).toEqual({ field: 'rights', order: 'desc' })
    }
  })

  it('falls back to createdAt for unsupported sort field', () => {
    const wrapper = mountTable({ items: [] })
    const vm = wrapper.vm as any
    vm.onSort({ sortField: 'password', sortOrder: 1 })
    const emitted = wrapper.emitted('sort')
    expect(emitted).toBeTruthy()
    if (emitted) {
      expect(emitted[emitted.length - 1][0]).toEqual({ field: 'createdAt', order: 'asc' })
    }
  })
})

describe('AdminUsersTable no action column', () => {
  it('does not render action/edit/delete buttons', () => {
    const items = [makeUser(1)]
    const wrapper = mountTable({ items })
    const text = wrapper.text()
    expect(text).not.toContain('Delete')
    expect(text).not.toContain('Edit')
    expect(text).not.toContain('Actions')
    expect(text).not.toContain('Block')
  })
})
