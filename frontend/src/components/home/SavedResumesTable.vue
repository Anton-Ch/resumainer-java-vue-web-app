<template>
  <div>
    <!-- Toolbar: search + filters -->
    <div class="table-toolbar">
      <div class="toolbar-row">
        <IconField>
          <InputIcon>
            <i class="pi pi-search" />
          </InputIcon>
          <InputText
            v-model="searchText"
            :placeholder="$t('home.table.searchPlaceholder')"
            @keyup="onSearchInput"
            class="search-input"
          />
        </IconField>
        <MultiSelect
          v-model="selectedLanguages"
          :options="languageOptions"
          optionLabel="label"
          optionValue="value"
          :placeholder="$t('home.table.language')"
          :maxSelectedLabels="2"
          :showToggleAll="false"
          class="filter-select"
          @change="onFiltersChange"
        />
        <MultiSelect
          v-model="selectedAdaptations"
          :options="adaptationOptions"
          optionLabel="label"
          optionValue="value"
          :placeholder="$t('home.table.adaptationLevel')"
          :maxSelectedLabels="3"
          :showToggleAll="false"
          class="filter-select"
          @change="onFiltersChange"
        />
        <Calendar
          v-model="selectedDate"
          :placeholder="$t('home.table.created')"
          dateFormat="yy-mm-dd"
          class="filter-date"
          @date-select="onFiltersChange"
          @clear="onFiltersChange"
          showClear
        />
      </div>
    </div>

    <!-- DataTable -->
    <DataTable
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
      :paginatorTemplate="paginatorTemplate"
      :currentPageReportTemplate="$t('home.table.pageReport')"
      tableStyle="min-width: 50rem"
      :responsiveLayout="isMobile ? 'scroll' : 'stack'"
    >
      <Column field="resumeTitle" :sortable="true" :header="$t('home.table.resumeTitle')" />
      <Column field="vacancy" :sortable="true" :header="$t('home.table.vacancy')">
        <template #body="{ data }">
          <span v-tooltip.top="data.vacancy" class="truncate-cell">{{ data.vacancy }}</span>
        </template>
      </Column>
      <Column field="company" :sortable="true" :header="$t('home.table.company')">
        <template #body="{ data }">
          <span v-tooltip.top="data.company" class="truncate-cell">{{ data.company }}</span>
        </template>
      </Column>
      <Column field="language" :sortable="true" :header="$t('home.table.language')">
        <template #body="{ data }">
          <Tag :value="data.language" :severity="data.language === 'EN' ? 'info' : 'success'" />
        </template>
      </Column>
      <Column field="adaptationLevel" :sortable="true" :header="$t('home.table.adaptationLevel')">
        <template #body="{ data }">
          <Tag :value="$t('adaptation.' + data.adaptationLevel.toLowerCase())" :severity="getAdaptationSeverity(data.adaptationLevel)" />
        </template>
      </Column>
      <Column field="createdAt" :sortable="true" :header="$t('home.table.created')" />
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-file" style="font-size: 2rem; color: #8091A7; margin-bottom: 0.5rem;"></i>
          <h3>{{ $t('home.table.emptyTitle') }}</h3>
          <p>{{ $t('home.table.noResultsText') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import Calendar from 'primevue/calendar'
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

const { t } = useI18n()

const isMobile = ref(window.innerWidth < 640)
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 640
})

const paginatorTemplate = computed(() =>
  isMobile.value
    ? 'PrevPageLink PageLinks NextPageLink RowsPerPageDropdown'
    : 'CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown'
)

const languageOptions = [
  { label: t('language.en'), value: 'EN' },
  { label: t('language.ru'), value: 'RU' }
]

const adaptationOptions = [
  { label: t('adaptation.minimal'), value: 'MINIMAL' },
  { label: t('adaptation.balanced'), value: 'BALANCED' },
  { label: t('adaptation.maximum'), value: 'MAXIMUM' }
]

// --- Filter state ---
const searchText = ref('')
const selectedLanguages = ref(['EN', 'RU'])
const selectedAdaptations = ref(['MINIMAL', 'BALANCED', 'MAXIMUM'])
const selectedDate = ref<Date | null>(null)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function onSearchInput(e: KeyboardEvent) {
  const target = e.target as HTMLInputElement
  const val = target.value || ''

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
    createdDate: selectedDate.value
  })
}

function onPage(event: any) {
  emit('page', event)
}

function onSort(event: any) {
  emit('sort', event)
}

function onRowClick(event: any) {
  emit('openResume', event.data as SavedResumeData)
}

function getAdaptationSeverity(level: string): 'success' | 'info' | 'warn' | undefined {
  switch (level) {
    case 'MAXIMUM': return 'success'
    case 'BALANCED': return 'info'
    case 'MINIMAL': return 'warn'
    default: return undefined
  }
}
</script>

<style scoped>
.table-toolbar {
  margin-bottom: 1rem;
}
.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}
.search-input {
  min-width: 260px;
}
.filter-select {
  min-width: 160px;
}
.filter-date {
  min-width: 150px;
}
.truncate-cell {
  display: block;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #5D718B;
}
.empty-state h3 {
  font-family: 'Manrope', sans-serif;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.empty-state p {
  margin: 0;
  font-size: 0.9rem;
}
@media (max-width: 639px) {
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input, .filter-select, .filter-date {
    min-width: 100%;
  }
}
</style>
