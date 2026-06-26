<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <div class="admin-header">
        <h1>{{ $t('admin.users.title') }}</h1>
        <p class="admin-subtitle">{{ $t('admin.users.subtitle') }}</p>
      </div>

      <!-- Loading skeleton -->
      <div v-if="loading && users.length === 0" class="skeleton-block">
        <Skeleton width="100%" height="200px" />
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="inline-error">
        <i class="pi pi-exclamation-triangle"></i>
        <span>{{ error }}</span>
        <Button icon="pi pi-refresh" :label="$t('admin.usersTable.retry')" class="p-button-text p-button-sm" @click="loadUsers" />
      </div>

      <!-- Table -->
      <AdminUsersTable
        v-else
        :items="users"
        :totalRecords="totalRecords"
        :loading="loading"
        :first="firstRow"
        :sortField="currentSortField"
        :sortOrder="currentSortOrder"
        :size="queryParams.size || 10"
        @page="onPage"
        @sort="onSort"
        @filter="onFilter"
        @search="onSearch"
        @navigate="onNavigate"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import AdminUsersTable from '@/components/admin/AdminUsersTable.vue'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import { getAdminUsers } from '@/services/adminService'
import type { AdminUserListItem } from '@/types/admin'

const router = useRouter()
const { t } = useI18n()

const users = ref<AdminUserListItem[]>([])
const totalRecords = ref(0)
const loading = ref(true)
const error = ref<string | null>(null)

const queryParams = reactive<{
  page: number
  size: number
  sort: string
  search?: string
  role?: string
  status?: string
  permission?: string
  rights?: string
  dateFrom?: string
  dateTo?: string
}>({
  page: 0,
  size: 10,
  sort: 'createdAt,desc'
})

const firstRow = computed(() => (queryParams.page || 0) * (queryParams.size || 10))

const currentSortField = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[0] || 'createdAt'
})

const currentSortOrder = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[1] === 'asc' ? 1 : -1
})

async function loadUsers() {
  loading.value = true
  error.value = null
  try {
    const data = await getAdminUsers({
      page: queryParams.page,
      size: queryParams.size,
      search: queryParams.search,
      role: queryParams.role,
      status: queryParams.status,
      permission: queryParams.permission,
      rights: queryParams.rights,
      createdFrom: queryParams.dateFrom,
      createdTo: queryParams.dateTo,
      sort: queryParams.sort
    })
    users.value = data.items
    totalRecords.value = data.totalElements
  } catch (e: any) {
    error.value = e.message || t('admin.usersTable.error')
    users.value = []
    totalRecords.value = 0
  } finally {
    loading.value = false
  }
}

function onPage(event: any) {
  queryParams.page = Math.floor(event.first / event.rows)
  queryParams.size = event.rows
  loadUsers()
}

const ALLOWED_SORT_FIELDS = new Set([
  'fullName', 'username', 'email', 'role', 'status',
  'generationPermission', 'rights', 'resumesCount', 'createdAt'
])

function onSort(event: any) {
  // event is { field, order } from AdminUsersTable
  const rawField = event.field || 'createdAt'
  const field = ALLOWED_SORT_FIELDS.has(rawField) ? rawField : 'createdAt'
  queryParams.sort = `${field},${event.order || 'desc'}`
  queryParams.page = 0
  loadUsers()
}

function onFilter(filters: any) {
  queryParams.role = filters.role || undefined
  queryParams.status = filters.status || undefined
  queryParams.permission = filters.permission || undefined
  queryParams.rights = filters.rights || undefined
  queryParams.dateFrom = filters.dateFrom || undefined
  queryParams.dateTo = filters.dateTo || undefined
  queryParams.page = 0
  loadUsers()
}

function onSearch(search: string) {
  queryParams.search = search || undefined
  queryParams.page = 0
  loadUsers()
}

function onNavigate(userId: string) {
  router.push(`/admin/users/${userId}`)
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.page-main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
.admin-header h1 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.admin-subtitle {
  color: var(--vue-text-secondary);
  font-size: var(--vue-text-sm);
  margin: 0;
}
.skeleton-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.inline-error {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #FFF7ED;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  color: #92400E;
  font-size: 0.9rem;
}
</style>
