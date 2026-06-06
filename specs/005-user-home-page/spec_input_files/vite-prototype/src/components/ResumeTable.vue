<template>
  <div>
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <p-inputtext v-model="globalSearch" :placeholder="t('home.table.searchPlaceholder')" @keyup="searchOnEnter" />
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ t('home.table.filterLanguage') }}</span>
          <p-multiselect v-model="selectedLanguages" :options="langOpts" :placeholder="t('home.table.language')" :maxSelectedLabels="2" :showToggleAll="false" :selectedItemsLabel="langAllSelected" optionLabel="label" optionValue="value" style="min-width:150px" />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ t('home.table.filterAdaptation') }}</span>
          <p-multiselect v-model="selectedAdaptations" :options="adaptOpts" :placeholder="t('home.table.adaptationLevel')" :maxSelectedLabels="2" :showToggleAll="false" :selectedItemsLabel="adaptAllSelected" optionLabel="label" optionValue="value" style="min-width:160px" />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ t('home.table.filterDate') }}</span>
          <div class="date-range-group">
            <p-calendar v-model="dateFrom" :placeholder="t('home.table.dateFrom')" dateFormat="yy-mm-dd" style="min-width:130px" />
            <span class="date-range-sep">–</span>
            <p-calendar v-model="dateTo" :placeholder="t('home.table.dateTo')" dateFormat="yy-mm-dd" style="min-width:130px" />
          </div>
        </div>
        <p-button v-if="showClear" :label="t('home.table.clear')" icon="pi pi-filter-slash" class="p-button-success p-button-outlined" v-tooltip.top="t('home.table.clearTooltip')" @click="onClear" />
      </div>
    </div>
    <p-datatable
      ref="dt"
      :value="filteredResumes"
      :paginator="true"
      :rows="rowsPerPage"
      :rowsPerPageOptions="[10,20,50]"
      :sortField="sortField"
      :sortOrder="sortOrder"
      @sort="onSort"
      :loading="tableLoading"
      dataKey="id"
      :removableSort="true"
      :currentPageReportTemplate="t('home.table.pageReport')"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :responsiveLayout="isMobile ? 'scroll' : 'stack'"
      @row-click="onRowClick"
      :globalFilterFields="['title','company','vacancy']"
      :emptyMessage="t('home.table.noResultsTitle')"
      @page="onPageChange"
    >
      <p-column field="title" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('title')">{{ t('home.table.resumeTitle') }}</span>
        </template>
        <template #body="s">{{ s.data.title }}</template>
      </p-column>
      <p-column field="vacancy" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('vacancy')">{{ t('home.table.vacancy') }}</span>
        </template>
        <template #body="s">
          <span v-tooltip.top="s.data.vacancy" class="truncate-cell" style="max-width:220px">{{ s.data.vacancy }}</span>
        </template>
      </p-column>
      <p-column field="company" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('company')">{{ t('home.table.company') }}</span>
        </template>
        <template #body="s">
          <span v-tooltip.top="s.data.company" class="truncate-cell" style="max-width:200px">{{ s.data.company }}</span>
        </template>
      </p-column>
      <p-column field="language" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('language')">{{ t('home.table.language') }}</span>
        </template>
        <template #body="s">
          <p-tag :value="s.data.language === 'English' ? 'EN' : 'RU'" :severity="s.data.language === 'English' ? 'info' : 'success'" />
        </template>
      </p-column>
      <p-column field="adaptationLevel" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('adaptationLevel')">{{ t('home.table.adaptationLevel') }}</span>
        </template>
        <template #body="s">
          <p-tag :value="t('adaptation.'+s.data.adaptationLevel.toLowerCase())" :severity="getAdaptationSeverity(s.data.adaptationLevel)" />
        </template>
      </p-column>
      <p-column field="created" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('created')">{{ t('home.table.created') }}</span>
        </template>
        <template #body="s">{{ s.data.created }}</template>
      </p-column>
    </p-datatable>
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'

const props = defineProps(['resumes'])
const emit = defineEmits(['openResume'])
const dt = ref(null)

const t = inject('t')
const lang = inject('lang')
const isMobile = inject('isMobile')

const globalSearch = ref('')
const selectedLanguages = ref(['English','Russian'])
const selectedAdaptations = ref(['Minimal','Balanced','Maximum'])
const dateFrom = ref(null)
const dateTo = ref(null)
const sortField = ref('created')
const sortOrder = ref(-1)
const tableLoading = ref(false)
const rowsPerPage = ref(10)

const defaultDateFrom = ref(null)
const defaultDateTo = ref(null)

// Prototype note: default date range is calculated from loaded resume data.
// In production, this data will come from the backend API.
// Do not hardcode specific dates here.
const initDefaultDates = () => {
  if (props.resumes.length === 0) {
    defaultDateFrom.value = null
    defaultDateTo.value = null
    dateFrom.value = null
    dateTo.value = null
    return
  }
  let earliest = null
  let latest = null
  for (const r of props.resumes) {
    if (!r.created) continue
    if (earliest === null || r.created < earliest) earliest = r.created
    if (latest === null || r.created > latest) latest = r.created
  }
  defaultDateFrom.value = earliest
  defaultDateTo.value = latest
  dateFrom.value = earliest
  dateTo.value = latest
}
onMounted(initDefaultDates)
watch(() => props.resumes.length, () => { initDefaultDates() })

const languageOpts = [
  { value: 'English', en: 'English', ru: 'Английский' },
  { value: 'Russian', en: 'Russian', ru: 'Русский' }
]
const adaptationOpts = [
  { value: 'Minimal', en: 'Minimal', ru: 'Минимальная' },
  { value: 'Balanced', en: 'Balanced', ru: 'Сбалансированная' },
  { value: 'Maximum', en: 'Maximum', ru: 'Максимальная' }
]

const langOpts = computed(() => languageOpts.map(o => ({ value: o.value, label: o[lang.value] })))
const adaptOpts = computed(() => adaptationOpts.map(o => ({ value: o.value, label: o[lang.value] })))

const adaptAllSelected = computed(() => {
  if (selectedAdaptations.value.length === 3) return lang.value === 'ru' ? 'Все уровни' : 'All levels'
  return undefined
})
const langAllSelected = computed(() => {
  if (selectedLanguages.value.length === 2) return lang.value === 'ru' ? 'Все языки' : 'All languages'
  return undefined
})

const fmtDate = (d) => {
  if (!d) return null
  const dt_val = d instanceof Date ? d : new Date(d)
  if (isNaN(dt_val.getTime())) return null
  return dt_val.getFullYear() + '-' + String(dt_val.getMonth()+1).padStart(2,'0') + '-' + String(dt_val.getDate()).padStart(2,'0')
}

const isDefault = computed(() => {
  if (globalSearch.value) return false
  if (selectedLanguages.value.length !== 2 || !selectedLanguages.value.includes('English') || !selectedLanguages.value.includes('Russian')) return false
  if (selectedAdaptations.value.length !== 3 || !selectedAdaptations.value.includes('Minimal') || !selectedAdaptations.value.includes('Balanced') || !selectedAdaptations.value.includes('Maximum')) return false
  if (fmtDate(dateFrom.value) !== defaultDateFrom.value) return false
  if (fmtDate(dateTo.value) !== defaultDateTo.value) return false
  if (sortField.value !== 'created' || sortOrder.value !== -1) return false
  if (rowsPerPage.value !== 10) return false
  return true
})
const showClear = computed(() => !isDefault.value)

const sortTooltip = (field) => {
  if (sortField.value !== field) return t('home.table.sortNotSorted')
  if (sortOrder.value === 1) return t('home.table.sortAsc')
  if (sortOrder.value === -1) return t('home.table.sortDesc')
  return t('home.table.sortNotSorted')
}

const filteredResumes = computed(() => {
  let result = [...props.resumes]
  if (globalSearch.value && globalSearch.value.trim().length >= 2) {
    const q = globalSearch.value.toLowerCase().trim()
    result = result.filter(r => r.title.toLowerCase().includes(q) || r.company.toLowerCase().includes(q) || r.vacancy.toLowerCase().includes(q))
  }
  if (selectedLanguages.value.length > 0 && selectedLanguages.value.length < 2) {
    result = result.filter(r => selectedLanguages.value.includes(r.language))
  }
  if (selectedAdaptations.value.length > 0 && selectedAdaptations.value.length < 3) {
    result = result.filter(r => selectedAdaptations.value.includes(r.adaptationLevel))
  }
  const fromStr = fmtDate(dateFrom.value)
  const toStr = fmtDate(dateTo.value)
  if (fromStr) result = result.filter(r => r.created >= fromStr)
  if (toStr) result = result.filter(r => r.created <= toStr)
  return result
})

const onSort = (e) => { sortField.value = e.sortField; sortOrder.value = e.sortOrder }
const onSearch = () => { tableLoading.value = true; setTimeout(() => { tableLoading.value = false }, 250) }
const searchOnEnter = (e) => { if (e.key === 'Enter') onSearch() }
const onRowClick = (e) => { emit('openResume', e.data) }
const onPageChange = () => {}

const onClear = () => {
  globalSearch.value = ''
  selectedLanguages.value = ['English','Russian']
  selectedAdaptations.value = ['Minimal','Balanced','Maximum']
  dateFrom.value = defaultDateFrom.value
  dateTo.value = defaultDateTo.value
  sortField.value = 'created'
  sortOrder.value = -1
  rowsPerPage.value = 10
  if (dt.value) {
    try { dt.value.resetPage() } catch(e) {}
  }
}

const getAdaptationSeverity = (level) => {
  switch(level) { case 'Maximum': return 'success'; case 'Balanced': return 'info'; case 'Minimal': return 'warning'; default: return 'info' }
}
</script>

<style scoped>
.truncate-cell{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;cursor:help}
</style>
