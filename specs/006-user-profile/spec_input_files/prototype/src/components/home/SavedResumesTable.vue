<template>
  <div>
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <InputText
          v-model="searchText"
          :placeholder="$t('home.table.searchPlaceholder')"
          @keyup="onSearchInput"
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ $t('home.table.filterLanguage') }}</span>
          <MultiSelect
            v-model="selectedLanguages"
            :options="langOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('home.table.language')"
            :maxSelectedLabels="2"
            :showToggleAll="false"
            :selectedItemsLabel="langAllSelectedLabel"
            class="filter-select"
            @change="onFiltersChange"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('home.table.filterAdaptation') }}</span>
          <MultiSelect
            v-model="selectedAdaptations"
            :options="adaptOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('home.table.adaptationLevel')"
            :maxSelectedLabels="2"
            :showToggleAll="false"
            :selectedItemsLabel="adaptAllSelectedLabel"
            class="filter-select"
            @change="onFiltersChange"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('home.table.filterDate') }}</span>
          <div class="date-range-group">
            <DatePicker
              v-model="dateFrom"
              :placeholder="$t('home.table.dateFrom')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
            <span class="date-range-sep">–</span>
            <DatePicker
              v-model="dateTo"
              :placeholder="$t('home.table.dateTo')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
          </div>
        </div>
        <Button
          v-if="showClear"
          :label="$t('home.table.clear')"
          icon="pi pi-filter-slash"
          class="p-button-success p-button-outlined"
          v-tooltip.top="$t('home.table.clearTooltip')"
          @click="onClear"
        />
      </div>
    </div>

    <DataTable
      ref="dt"
      :value="resumes"
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
      :currentPageReportTemplate="$t('home.table.pageReport')"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :responsiveLayout="isMobile ? 'scroll' : 'stack'"
      :emptyMessage="$t('home.table.noResultsTitle')"
    >
      <Column field="resumeTitle" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('resumeTitle')">{{ $t('home.table.resumeTitle') }}</span>
        </template>
      </Column>
      <Column field="vacancy" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('vacancy')">{{ $t('home.table.vacancy') }}</span>
        </template>
        <template #body="{ data }">
          <span v-tooltip.top="data.vacancy" class="truncate-cell" style="max-width:220px">{{ data.vacancy }}</span>
        </template>
      </Column>
      <Column field="company" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('company')">{{ $t('home.table.company') }}</span>
        </template>
        <template #body="{ data }">
          <span v-tooltip.top="data.company" class="truncate-cell" style="max-width:200px">{{ data.company }}</span>
        </template>
      </Column>
      <Column field="language" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('language')">{{ $t('home.table.language') }}</span>
        </template>
        <template #body="{ data }">
          <Tag :value="data.language === 'EN' ? 'EN' : 'RU'" :severity="data.language === 'EN' ? 'info' : 'success'" />
        </template>
      </Column>
      <Column field="adaptationLevel" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('adaptationLevel')">{{ $t('home.table.adaptationLevel') }}</span>
        </template>
        <template #body="{ data }">
          <Tag :value="$t('adaptation.' + data.adaptationLevel.toLowerCase())" :severity="getAdaptationSeverity(data.adaptationLevel)" />
        </template>
      </Column>
      <Column field="createdAt" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('createdAt')">{{ $t('home.table.created') }}</span>
        </template>
      </Column>
      <template #empty>
        <div class="empty-state">
          <div class="empty-icon"><i class="pi pi-file"></i></div>
          <h3>{{ $t('home.table.emptyTitle') }}</h3>
          <p>{{ $t('home.table.noResultsText') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import type { SavedResumeData } from '@/services/userHomeService'

const props = defineProps<{
  resumes: SavedResumeData[]
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
  openResume: [resume: SavedResumeData]
}>()

const { t, locale } = useI18n()
const dt = ref()

// Reactive options — update when locale changes
const languageOptions = [
  { value: 'EN', en: 'English', ru: 'Английский' },
  { value: 'RU', en: 'Russian', ru: 'Русский' }
]
const adaptationOptions = [
  { value: 'MINIMAL', en: 'Minimal', ru: 'Минимальная' },
  { value: 'BALANCED', en: 'Balanced', ru: 'Сбалансированная' },
  { value: 'MAXIMUM', en: 'Maximum', ru: 'Максимальная' }
]

const langOptions = computed(() =>
  languageOptions.map(o => ({ value: o.value, label: o[locale.value as keyof typeof o] }))
)
const adaptOptions = computed(() =>
  adaptationOptions.map(o => ({ value: o.value, label: o[locale.value as keyof typeof o] }))
)

const langAllSelectedLabel = computed(() => {
  if (selectedLanguages.value.length === 2) return locale.value === 'ru' ? 'Все языки' : 'All languages'
  return undefined
})
const adaptAllSelectedLabel = computed(() => {
  if (selectedAdaptations.value.length === 3) return locale.value === 'ru' ? 'Все уровни' : 'All levels'
  return undefined
})

const isMobile = ref(false)

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

// --- Filter state ---
const searchText = ref('')
const selectedLanguages = ref(['EN', 'RU'])
const selectedAdaptations = ref(['MINIMAL', 'BALANCED', 'MAXIMUM'])
const dateFrom = ref<Date | null>(null)
const dateTo = ref<Date | null>(null)

// Track default date range for Clear
const defaultDateFrom = ref<Date | null>(null)
const defaultDateTo = ref<Date | null>(null)

// Compute whether filters are in default state
const showClear = computed(() => {
  if (searchText.value) return true
  if (selectedLanguages.value.length !== 2) return true
  if (selectedAdaptations.value.length !== 3) return true
  if (dateFrom.value !== null) return true
  if (dateTo.value !== null) return true
  return false
})

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
    debounceTimer = setTimeout(() => {
      emit('search', val)
    }, 300)
  }
}

function onFiltersChange() {
  emit('filter', {
    language: selectedLanguages.value,
    adaptationLevel: selectedAdaptations.value,
    dateFrom: formatDate(dateFrom.value),
    dateTo: formatDate(dateTo.value)
  })
}

function onClear() {
  searchText.value = ''
  selectedLanguages.value = ['EN', 'RU']
  selectedAdaptations.value = ['MINIMAL', 'BALANCED', 'MAXIMUM']
  dateFrom.value = defaultDateFrom.value
  dateTo.value = defaultDateTo.value
  try { dt.value?.resetPage() } catch {}
  onFiltersChange()
}

function onPage(event: any) { emit('page', event) }
function onSort(event: any) { emit('sort', event) }
function onRowClick(event: any) { emit('openResume', event.data as SavedResumeData) }

function getAdaptationSeverity(level: string): 'success' | 'info' | 'warn' | undefined {
  switch (level) {
    case 'MAXIMUM': return 'success'
    case 'BALANCED': return 'info'
    case 'MINIMAL': return 'warn'
    default: return undefined
  }
}

function sortTooltip(field: string): string {
  const sf = props.sortField
  const so = props.sortOrder
  if (sf !== field) return t('home.table.sortNotSorted')
  if (so === 1) return t('home.table.sortAsc')
  if (so === -1) return t('home.table.sortDesc')
  return t('home.table.sortNotSorted')
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
