<template>
  <div class="admin-users-table">
    <!-- Toolbar -->
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <InputText
          v-model="searchText"
          :placeholder="$t('admin.usersTable.searchPlaceholder')"
          @keyup="onSearchInput"
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.usersTable.role') }}</span>
          <Select
            v-model="selectedRole"
            :options="roleOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('admin.usersTable.all')"
            class="filter-select"
            @change="onFiltersChange"
            :showClear="true"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.usersTable.status') }}</span>
          <Select
            v-model="selectedStatus"
            :options="statusOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('admin.usersTable.all')"
            class="filter-select"
            @change="onFiltersChange"
            :showClear="true"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.usersTable.permission') }}</span>
          <Select
            v-model="selectedPermission"
            :options="permissionOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('admin.usersTable.all')"
            class="filter-select"
            @change="onFiltersChange"
            :showClear="true"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.usersTable.rights') }}</span>
          <Select
            v-model="selectedRights"
            :options="rightsOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('admin.usersTable.all')"
            class="filter-select"
            @change="onFiltersChange"
            :showClear="true"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.usersTable.created') }}</span>
          <div class="date-range-group">
            <DatePicker
              v-model="dateFrom"
              :placeholder="$t('admin.usersTable.dateFrom')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
            <span class="date-range-sep">–</span>
            <DatePicker
              v-model="dateTo"
              :placeholder="$t('admin.usersTable.dateTo')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              :minDate="dateFrom || undefined"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
          </div>
          <span v-if="dateError" class="date-error">{{ dateError }}</span>
        </div>
        <Button
          v-if="showClear"
          :label="$t('admin.usersTable.clear')"
          icon="pi pi-filter-slash"
          class="p-button-success p-button-outlined"
          v-tooltip.top="$t('admin.usersTable.clearTooltip')"
          @click="onClear"
        />
      </div>
    </div>

    <!-- DataTable -->
    <DataTable
      ref="dt"
      :value="items"
      lazy
      paginator
      :first="first"
      :rows="size"
      :rowsPerPageOptions="[10, 20, 50]"
      dataKey="id"
      :totalRecords="totalRecords"
      :loading="loading"
      :removableSort="true"
      :sortField="sortField"
      :sortOrder="sortOrder"
      @page="onPage"
      @sort="onSort"
      @row-click="onRowClick"
      :currentPageReportTemplate="pageReportTemplate"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :emptyMessage="$t('admin.usersTable.noResults')"
      selectionMode="single"
    >
      <Column field="fullName" :sortable="true" :header="$t('admin.usersTable.fullName')">
        <template #body="{ data }">
          <span v-tooltip.top="data.fullName" class="truncate-cell" style="max-width:180px">{{ data.fullName || '-' }}</span>
        </template>
      </Column>
      <Column field="username" :sortable="true" :header="$t('admin.usersTable.username')" />
      <Column field="email" :sortable="true" :header="$t('admin.usersTable.email')">
        <template #body="{ data }">
          <span v-tooltip.top="data.email" class="truncate-cell" style="max-width:220px">{{ data.email }}</span>
        </template>
      </Column>
      <Column field="role" :sortable="true" :header="$t('admin.usersTable.role')">
        <template #body="{ data }">
          <Tag :value="data.roleName" :severity="data.roleCode === 'ADMIN' ? 'info' : 'success'" />
        </template>
      </Column>
      <Column field="status" :sortable="true" :header="$t('admin.usersTable.status')">
        <template #body="{ data }">
          <Tag :value="data.statusName" :severity="data.statusCode === 'ACTIVE' ? 'success' : 'danger'" />
        </template>
      </Column>
      <Column field="generationPermission" :sortable="true" :header="$t('admin.usersTable.permission')">
        <template #body="{ data }">
          <Tag :value="data.permissionName" :severity="data.permissionCode === 'ALLOWED' ? 'success' : 'warn'" />
        </template>
      </Column>
      <Column field="rights" :sortable="true" :header="$t('admin.usersTable.rights')">
        <template #body="{ data }">
          <Tag :value="data.isPrivileged ? $t('admin.usersTable.privileged') : $t('admin.usersTable.nonPrivileged')" :severity="data.isPrivileged ? 'info' : 'warn'" />
        </template>
      </Column>
      <Column field="resumesCount" :sortable="true" :header="$t('admin.usersTable.resumesCount')" />
      <Column field="createdAt" :sortable="true" :header="$t('admin.usersTable.created')">
        <template #body="{ data }">
          <span>{{ formatDateTime(data.createdAt) }}</span>
        </template>
      </Column>
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-users" style="font-size:2.5rem;color:#8091A7;margin-bottom:0.5rem;display:block;"></i>
          <h3>{{ $t('admin.usersTable.emptyTitle') }}</h3>
          <p>{{ $t('admin.usersTable.emptyText') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import type { AdminUserListItem } from '@/types/admin'

const ALLOWED_SORT_FIELDS = new Set([
  'fullName', 'username', 'email', 'role', 'status',
  'generationPermission', 'rights', 'resumesCount', 'createdAt'
])

defineProps<{
  items: AdminUserListItem[]
  totalRecords: number
  loading: boolean
  first: number
  sortField: string
  sortOrder: number
  size: number
}>()

const emit = defineEmits<{
  page: [event: any]
  sort: [event: any]
  filter: [filters: any]
  search: [query: string]
  navigate: [userId: string]
}>()

const { t, locale } = useI18n()
const dt = ref()

const pageReportTemplate = computed(() =>
  locale.value === 'ru'
    ? '{first}–{last} из {totalRecords}'
    : 'Showing {first} to {last} of {totalRecords}'
)

function formatDateTime(value: string | null | undefined): string {
  if (!value) return '-'
  if (value.length >= 16 && value.includes('T')) {
    return `${value.slice(0, 10)}_${value.slice(11, 16)}`
  }
  return value
}

const roleOptions = computed(() => [
  { value: 'USER', label: locale.value === 'ru' ? 'Пользователь' : 'User' },
  { value: 'ADMIN', label: locale.value === 'ru' ? 'Администратор' : 'Admin' },
])

const statusOptions = computed(() => [
  { value: 'ACTIVE', label: locale.value === 'ru' ? 'Активен' : 'Active' },
  { value: 'BLOCKED', label: locale.value === 'ru' ? 'Заблокирован' : 'Blocked' },
])

const permissionOptions = computed(() => [
  { value: 'ALLOWED', label: locale.value === 'ru' ? 'Разрешено' : 'Allowed' },
  { value: 'FORBIDDEN', label: locale.value === 'ru' ? 'Запрещено' : 'Forbidden' },
])

const rightsOptions = computed(() => [
  { value: 'PRIVILEGED', label: locale.value === 'ru' ? 'Привилегированный' : 'Privileged' },
  { value: 'NON_PRIVILEGED', label: locale.value === 'ru' ? 'Обычный' : 'Non-privileged' },
])

const searchText = ref('')
const selectedRole = ref<string | null>(null)
const selectedStatus = ref<string | null>(null)
const selectedPermission = ref<string | null>(null)
const selectedRights = ref<string | null>(null)
const dateFrom = ref<Date | null>(null)
const dateTo = ref<Date | null>(null)
const dateError = ref<string | null>(null)

const showClear = computed(() =>
  !!searchText.value || !!selectedRole.value || !!selectedStatus.value
  || !!selectedPermission.value || !!selectedRights.value
  || dateFrom.value !== null || dateTo.value !== null
)

function formatDate(d: Date | null): string | null {
  if (!d) return null
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function onSearchInput(e: KeyboardEvent) {
  const val = (e.target as HTMLInputElement).value || ''
  if (debounceTimer) clearTimeout(debounceTimer)
  if (val.length >= 3 || val.length === 0) {
    debounceTimer = setTimeout(() => emit('search', val), 300)
  }
}

function onFiltersChange() {
  dateError.value = null
  if (dateFrom.value && dateTo.value && dateTo.value < dateFrom.value) {
    dateError.value = t('admin.usersTable.dateError')
    return
  }
  emit('filter', {
    role: selectedRole.value,
    status: selectedStatus.value,
    permission: selectedPermission.value,
    rights: selectedRights.value,
    dateFrom: formatDate(dateFrom.value),
    dateTo: formatDate(dateTo.value)
  })
}

function onClear() {
  searchText.value = ''
  selectedRole.value = null
  selectedStatus.value = null
  selectedPermission.value = null
  selectedRights.value = null
  dateFrom.value = null
  dateTo.value = null
  dateError.value = null
  try { dt.value?.resetPage() } catch {}
  onFiltersChange()
}

function onPage(event: any) { emit('page', event) }

function onSort(event: any) {
  const rawField = event.sortField || 'createdAt'
  const field = ALLOWED_SORT_FIELDS.has(rawField) ? rawField : 'createdAt'
  const order = event.sortOrder === -1 ? 'desc' : 'asc'
  emit('sort', { field, order })
}

function onRowClick(event: any) {
  const user = event.data as AdminUserListItem
  emit('navigate', user.id)
}
</script>

<style scoped>
.admin-users-table { width: 100%; }
.table-toolbar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.search-field {
  position: relative;
  flex: 1;
  min-width: 220px;
  max-width: 420px;
}
.search-field .pi {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #8091A7;
  font-size: 0.9rem;
  z-index: 1;
  pointer-events: none;
}
.search-field .p-inputtext { padding-left: 40px !important; width: 100%; }
.filter-group {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  align-items: flex-end;
}
.filter-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.filter-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.search-input { min-width: 220px; }
.filter-select { min-width: 130px; }
.filter-date { min-width: 120px; }
.date-range-group {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
.date-range-sep { color: #8091A7; font-size: 0.9rem; }
.date-error {
  font-size: 0.75rem;
  color: #DC2626;
  margin-top: 4px;
}
.truncate-cell {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}
.empty-state { text-align: center; padding: 2rem; }
.empty-state h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.1rem;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.empty-state p { margin: 0; color: #5D718B; font-size: 0.9rem; }
.admin-users-table :deep(tr[data-p-selectable-row="true"]) { cursor: pointer; }
.admin-users-table :deep(tr[data-p-selectable-row="true"]:hover) { background-color: #F3F4F6; }
@media (max-width: 640px) {
  .table-toolbar { flex-direction: column; align-items: stretch; }
  .search-field { max-width: none; }
  .filter-group { flex-direction: column; align-items: stretch; }
  .filter-field { width: 100%; }
  .date-range-group { flex-direction: column; gap: 6px; }
  .date-range-sep { display: none; }
  .filter-select, .filter-date { min-width: 100%; width: 100%; }
}
</style>
