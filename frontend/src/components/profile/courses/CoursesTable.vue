<template>
  <div>
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <InputText
          v-model="searchInput"
          :placeholder="$t('profile.courses.searchPlaceholder')"
          @keyup="onSearchInput"
          class="search-input"
        />
        <small v-if="searchInput.length > 0 && searchInput.length < 3" class="search-hint">{{ $t('profile.courses.searchMinChars') }}</small>
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ $t('profile.courses.dateFrom') }}</span>
          <DatePicker v-model="dateFrom" class="filter-date" :maxDate="dateTo || undefined" @date-select="onFilterChange" showClear />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('profile.courses.dateTo') }}</span>
          <DatePicker v-model="dateTo" class="filter-date" :minDate="dateFrom || undefined" @date-select="onFilterChange" showClear />
        </div>
      </div>
      <Button
        v-if="showClear"
        :label="$t('profile.courses.resetFilters')"
        icon="pi pi-filter-slash"
        class="p-button-success p-button-outlined"
        @click="onClear"
      />
      <Button :label="$t('profile.courses.add')" icon="pi pi-plus" class="p-button-success" @click="$emit('add')" style="margin-left: auto;" />
    </div>
    <div v-if="dateFilterError" class="filter-error">{{ dateFilterError }}</div>

    <DataTable
      :value="data"
      :lazy="true"
      :totalRecords="totalRecords"
      :loading="loading"
      :paginator="true"
      :rows="rowsPerPage"
      :rowsPerPageOptions="[10, 20, 50]"
      :first="firstRecord"
      @page="onPage"
      @sort="onSort"
      @row-click="onRowClick"
      dataKey="id"
      :currentPageReportTemplate="$t('profile.courses.pageReport')"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :emptyMessage="$t('profile.courses.emptyTitle')"
      :sortField="sortField"
      :sortOrder="sortOrder"
      :removableSort="true"
      :responsiveLayout="isMobile ? 'scroll' : 'stack'"
    >
      <Column field="courseName" :sortable="true" :header="$t('profile.courses.courseColumn')" />
      <Column field="provider" :sortable="true" :header="$t('profile.courses.providerColumn')" />
      <Column field="startDate" :sortable="true" :header="$t('profile.courses.startDate')">
        <template #body="{ data }">
          {{ formatDate(data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" :sortable="true" :header="$t('profile.courses.endDate')">
        <template #body="{ data }">
          {{ formatDate(data.endDate) }}
        </template>
      </Column>
      <Column field="courseFocus" :sortable="true" :header="$t('profile.courses.skillsColumn')">
        <template #body="{ data }">
          <span v-tooltip.top="data.courseFocus" class="skills-cell">{{ truncateSkills(data.courseFocus) }}</span>
        </template>
      </Column>
      <template #empty>
        <div class="empty-state">
          <div class="empty-icon"><i class="pi pi-book"></i></div>
          <h3>{{ $t('profile.courses.emptyTitle') }}</h3>
          <p>{{ $t('profile.courses.emptyHint') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import type { Course } from '@/types/profile'

defineProps<{
  data: Course[]
  totalRecords: number
  loading: boolean
}>()

const emit = defineEmits<{
  add: []
  open: [course: Course]
  'update:params': [params: {
    page: number
    size: number
    sort: string
    order: string
    search: string
    dateFrom: string
    dateTo: string
  }]
}>()

const { t, locale } = useI18n()

const searchInput = ref('')
const searchText = ref('')
const dateFrom = ref<Date | null>(null)
const dateTo = ref<Date | null>(null)
const rowsPerPage = ref(10)
const sortField = ref('start_date')
const sortOrder = ref(-1)
const firstRecord = ref(0)
const currentPage = ref(0)
const isMobile = ref(false)
const dateFilterError = ref('')

function updateIsMobile() {
  isMobile.value = window.innerWidth < 640
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
})

function emitParams() {
  emit('update:params', {
    page: currentPage.value,
    size: rowsPerPage.value,
    sort: sortField.value,
    order: sortOrder.value === 1 ? 'asc' : 'desc',
    search: searchText.value,
    dateFrom: dateFrom.value ? dateFrom.value.toISOString().split('T')[0] : '',
    dateTo: dateTo.value ? dateTo.value.toISOString().split('T')[0] : ''
  })
}

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function truncateSkills(skills: string): string {
  if (!skills) return ''
  return skills.length > 40 ? skills.substring(0, 37) + '...' : skills
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function onSearchInput(e: KeyboardEvent) {
  const val = (e.target as HTMLInputElement).value || ''
  if (debounceTimer) clearTimeout(debounceTimer)
  if (val.length >= 3 || val.length === 0) {
    debounceTimer = setTimeout(() => {
      searchText.value = val
      currentPage.value = 0
      firstRecord.value = 0
      emitParams()
    }, 300)
  }
}

function onFilterChange() {
  if (dateFrom.value && dateTo.value && dateTo.value < dateFrom.value) {
    dateFilterError.value = t('profile.courses.dateFilterError')
    return
  }
  dateFilterError.value = ''
  currentPage.value = 0
  firstRecord.value = 0
  emitParams()
}

function onPage(event: any) {
  currentPage.value = event.page
  firstRecord.value = event.first
  rowsPerPage.value = event.rows
  emitParams()
}

function onSort(event: any) {
  if (event.sortOrder === 0) {
    sortField.value = 'start_date'
    sortOrder.value = -1
  } else {
    sortField.value = event.sortField || 'start_date'
    sortOrder.value = event.sortOrder
  }
  currentPage.value = 0
  firstRecord.value = 0
  emitParams()
}

const showClear = computed(() => {
  return !!(searchText.value || dateFrom.value !== null || dateTo.value !== null || sortField.value !== 'start_date')
})

function onClear() {
  searchInput.value = ''
  searchText.value = ''
  dateFrom.value = null
  dateTo.value = null
  sortField.value = 'start_date'
  sortOrder.value = -1
  dateFilterError.value = ''
  currentPage.value = 0
  firstRecord.value = 0
  rowsPerPage.value = 10
  emitParams()
}

watch(dateFrom, () => {
  if (dateFrom.value && dateTo.value && dateTo.value < dateFrom.value) {
    dateTo.value = null
  }
  dateFilterError.value = ''
})

function onRowClick(event: any) {
  emit('open', event.data as Course)
}
</script>

<style scoped>
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
  min-width: 200px;
  max-width: 360px;
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
.search-field .p-inputtext {
  padding-left: 40px !important;
  width: 100%;
}
.search-hint {
  position: absolute;
  bottom: -18px;
  left: 0;
  font-size: 11px;
  color: var(--vue-accent-warning);
  white-space: nowrap;
}
.filter-group {
  display: flex;
  gap: 10px;
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
.search-input {
  min-width: 200px;
}
.filter-date {
  min-width: 130px;
}
.filter-error {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-accent-error);
  margin-bottom: 12px;
}
.skills-cell {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
  cursor: help;
}
.empty-state {
  text-align: center;
  padding: 2rem;
}
.empty-icon {
  font-size: 2.5rem;
  color: #8091A7;
  margin-bottom: 0.5rem;
}
.empty-state h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.1rem;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.empty-state p {
  margin: 0;
  color: #5D718B;
  font-size: 0.9rem;
}
@media (max-width: 640px) {
  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-field {
    max-width: none;
  }
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-date {
    min-width: 100%;
    width: 100%;
  }
}
</style>
