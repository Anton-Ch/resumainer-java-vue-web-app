<template>
  <div class="admin-resumes-table">
    <!-- Toolbar: search + filters -->
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <InputText
          v-model="searchText"
          :placeholder="$t('admin.resumes.searchPlaceholder')"
          @keyup="onSearchInput"
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.resumes.language') }}</span>
          <Select
            v-model="selectedLanguage"
            :options="langOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('admin.resumes.language')"
            class="filter-select"
            @change="onFiltersChange"
            :showClear="true"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.resumes.adaptationLevel') }}</span>
          <Select
            v-model="selectedAdaptation"
            :options="adaptOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('admin.resumes.adaptationLevel')"
            class="filter-select"
            @change="onFiltersChange"
            :showClear="true"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('admin.resumes.created') }}</span>
          <div class="date-range-group">
            <DatePicker
              v-model="dateFrom"
              :placeholder="$t('admin.resumes.dateFrom')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
            <span class="date-range-sep">–</span>
            <DatePicker
              v-model="dateTo"
              :placeholder="$t('admin.resumes.dateTo')"
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
          :label="$t('admin.resumes.clear')"
          icon="pi pi-filter-slash"
          class="p-button-success p-button-outlined"
          v-tooltip.top="$t('admin.resumes.clearTooltip')"
          @click="onClear"
        />
      </div>
    </div>

    <!-- Data Table -->
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
      :currentPageReportTemplate="$t('admin.resumes.pageReport')"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :emptyMessage="$t('admin.resumes.noResults')"
      selectionMode="single"
    >
      <Column field="resumeTitle" :sortable="true" :header="$t('admin.resumes.resumeTitle')" />
      <Column field="vacancyTitle" :sortable="true" :header="$t('admin.resumes.vacancy')">
        <template #body="{ data }">
          <span v-tooltip.top="data.vacancyTitle" class="truncate-cell" style="max-width:200px">{{ data.vacancyTitle }}</span>
        </template>
      </Column>
      <Column field="companyName" :sortable="true" :header="$t('admin.resumes.company')">
        <template #body="{ data }">
          <span v-tooltip.top="data.companyName" class="truncate-cell" style="max-width:180px">{{ data.companyName }}</span>
        </template>
      </Column>
      <Column field="ownerUsername" :sortable="true" :header="$t('admin.resumes.owner')">
        <template #body="{ data }">
          <span v-tooltip.top="data.ownerUsername + (data.ownerFullName ? ' — ' + data.ownerFullName : '')">
            {{ data.ownerUsername }}
          </span>
        </template>
      </Column>
      <Column field="ownerEmail" :sortable="true" :header="$t('admin.resumes.email')">
        <template #body="{ data }">
          <span v-tooltip.top="data.ownerEmail" class="truncate-cell" style="max-width:200px">{{ data.ownerEmail }}</span>
        </template>
      </Column>
      <Column field="languageCode" :sortable="true" :header="$t('admin.resumes.language')">
        <template #body="{ data }">
          <Tag :value="data.languageCode" :severity="data.languageCode === 'EN' ? 'info' : 'success'" />
        </template>
      </Column>
      <Column field="adaptationLevel" :sortable="true" :header="$t('admin.resumes.adaptationLevel')">
        <template #body="{ data }">
          <Tag :value="$t('adaptation.' + data.adaptationLevel.toLowerCase())" :severity="tagSeverity(data.adaptationLevel)" />
        </template>
      </Column>
      <Column field="createdAt" :sortable="true" :header="$t('admin.resumes.created')" />
      <Column field="pdfAvailable" :header="$t('admin.resumes.pdfStatus')">
        <template #body="{ data }">
          <Tag v-if="data.pdfAvailable" value="READY" severity="success" />
          <Tag v-else :value="data.pdfStatus || 'N/A'" severity="warn" />
        </template>
      </Column>
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-file" style="font-size:2.5rem;color:#8091A7;margin-bottom:0.5rem;display:block;"></i>
          <h3>{{ $t('admin.resumes.emptyTitle') }}</h3>
          <p>{{ $t('admin.resumes.emptyText') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import type { AdminSavedResume } from '@/types/admin'

const props = defineProps<{
  items: AdminSavedResume[]
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
  openResume: [resume: AdminSavedResume]
}>()

const { t, locale } = useI18n()
const dt = ref()

const langOptions = computed(() => [
  { value: 'EN', label: locale.value === 'ru' ? 'Английский' : 'English' },
  { value: 'RU', label: locale.value === 'ru' ? 'Русский' : 'Russian' }
])

const adaptOptions = computed(() => [
  { value: 'MINIMAL', label: t('adaptation.minimal') },
  { value: 'BALANCED', label: t('adaptation.balanced') },
  { value: 'MAXIMUM', label: t('adaptation.maximum') }
])

// --- Filter state ---
const searchText = ref('')
const selectedLanguage = ref<string | null>(null)
const selectedAdaptation = ref<string | null>(null)
const dateFrom = ref<Date | null>(null)
const dateTo = ref<Date | null>(null)

const showClear = computed(() =>
  !!searchText.value || !!selectedLanguage.value || !!selectedAdaptation.value
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

const dateError = ref<string | null>(null)

function onFiltersChange() {
  dateError.value = null
  // Validate date range: if both dates set and to < from, block request
  if (dateFrom.value && dateTo.value && dateTo.value < dateFrom.value) {
    dateError.value = t('admin.resumes.dateError')
    return
  }
  emit('filter', {
    language: selectedLanguage.value,
    adaptationLevel: selectedAdaptation.value,
    dateFrom: formatDate(dateFrom.value),
    dateTo: formatDate(dateTo.value)
  })
}

function onClear() {
  searchText.value = ''
  selectedLanguage.value = null
  selectedAdaptation.value = null
  dateFrom.value = null
  dateTo.value = null
  dateError.value = null
  try { dt.value?.resetPage() } catch {}
  onFiltersChange()
}

function onPage(event: any) { emit('page', event) }
function onSort(event: any) { emit('sort', event) }
function onRowClick(event: any) { emit('openResume', event.data as AdminSavedResume) }

function tagSeverity(level: string): 'success' | 'info' | 'warn' | undefined {
  switch (level) {
    case 'MAXIMUM': return 'success'
    case 'BALANCED': return 'info'
    case 'MINIMAL': return 'warn'
    default: return undefined
  }
}
</script>

<style scoped>
.admin-resumes-table {
  width: 100%;
}
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
.search-field .p-inputtext {
  padding-left: 40px !important;
  width: 100%;
}
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
.search-input {
  min-width: 220px;
}
.filter-select {
  min-width: 160px;
}
.filter-date {
  min-width: 130px;
}
.date-range-group {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
.date-range-sep {
  color: #8091A7;
  font-size: 0.9rem;
}
.truncate-cell {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}
.empty-state {
  text-align: center;
  padding: 2rem;
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
.admin-resumes-table :deep(tr[data-p-selectable-row="true"]) {
  cursor: pointer;
}
.admin-resumes-table :deep(tr[data-p-selectable-row="true"]:hover) {
  background-color: #F3F4F6;
}
.date-error {
  font-size: 0.75rem;
  color: #DC2626;
  margin-top: 4px;
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
  .filter-field {
    width: 100%;
  }
  .date-range-group {
    flex-direction: column;
    gap: 6px;
  }
  .date-range-sep {
    display: none;
  }
  .filter-select, .filter-date {
    min-width: 100%;
    width: 100%;
  }
}
</style>
