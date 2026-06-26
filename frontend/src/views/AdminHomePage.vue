<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <h1 class="page-h1">{{ $t('admin.home.title') }}</h1>

      <!-- Dashboard stats -->
      <div v-if="dashboardLoading" class="skeleton-block">
        <div class="stats-skeleton-row">
          <Skeleton width="100%" height="100px" v-for="i in 4" :key="i" />
        </div>
      </div>
      <div v-else-if="dashboardError" class="inline-error">
        <i class="pi pi-exclamation-triangle"></i>
        <span>{{ dashboardError }}</span>
        <Button icon="pi pi-refresh" :label="$t('admin.home.retry')" class="p-button-text p-button-sm" @click="loadDashboard" />
      </div>
      <AdminStatsCards
        v-else-if="dashboard"
        :totalUsers="dashboard.totalUsers"
        :totalResumes="dashboard.totalResumes"
        :totalTokensSent="dashboard.totalTokensSent"
        :totalTokensSentWip="dashboard.totalTokensSentWip"
        :totalTokensGenerated="dashboard.totalTokensGenerated"
        :totalTokensGeneratedWip="dashboard.totalTokensGeneratedWip"
      />

      <!-- Quick actions -->
      <AdminQuickActions @scrollToResumes="scrollToResumes" />

      <!-- Admin Resumes section (anchored) -->
      <div ref="resumesSection" class="section-header">
        <h2>{{ $t('admin.resumes.title') }}</h2>
        <div v-if="tableError" class="inline-error" style="margin-bottom:0.5rem;">
          <i class="pi pi-exclamation-triangle"></i>
          <span>{{ tableError }}</span>
          <Button icon="pi pi-refresh" :label="$t('admin.home.retry')" class="p-button-text p-button-sm" @click="loadResumes" />
        </div>
      </div>

      <div v-if="tableLoading && resumes.length === 0" class="skeleton-block">
        <Skeleton width="100%" height="200px" />
      </div>
      <AdminResumesTable
        v-else
        :items="resumes"
        :totalRecords="totalRecords"
        :loading="tableLoading"
        :first="firstRow"
        :sortField="currentSortField"
        :sortOrder="currentSortOrder"
        :size="queryParams.size || 10"
        @page="onPage"
        @sort="onSort"
        @filter="onFilter"
        @search="onSearch"
        @openResume="openResumeModal"
      />

      <!-- Resume Details Modal -->
      <AdminResumeDetailsDialog
        v-model:visible="modalVisible"
        :resume="selectedResume"
        :delete-loading="deleteLoading"
        @delete="handleDelete"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import AdminStatsCards from '@/components/admin/AdminStatsCards.vue'
import AdminQuickActions from '@/components/admin/AdminQuickActions.vue'
import AdminResumesTable from '@/components/admin/AdminResumesTable.vue'
import AdminResumeDetailsDialog from '@/components/admin/AdminResumeDetailsDialog.vue'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import { getAdminDashboard, getAdminResumes, deleteAdminResume } from '@/services/adminService'
import type { AdminDashboard, AdminSavedResume } from '@/types/admin'

const toast = useToast()
const { t } = useI18n()

// --- Dashboard state ---
const dashboard = ref<AdminDashboard | null>(null)
const dashboardLoading = ref(true)
const dashboardError = ref<string | null>(null)

// --- Table state ---
const resumes = ref<AdminSavedResume[]>([])
const totalRecords = ref(0)
const tableLoading = ref(true)
const tableError = ref<string | null>(null)

const queryParams = reactive<{
  page: number
  size: number
  sort: string
  search?: string
  language?: string
  adaptationLevel?: string
  dateFrom?: string
  dateTo?: string
}>({
  page: 0,
  size: 10,
  sort: 'createdAt,desc'
})

// --- Modal state ---
const selectedResume = ref<AdminSavedResume | null>(null)
const modalVisible = ref(false)
const deleteLoading = ref(false)

// --- Refs ---
const resumesSection = ref<HTMLElement | null>(null)

const firstRow = computed(() => (queryParams.page || 0) * (queryParams.size || 10))

const currentSortField = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[0] || 'createdAt'
})

const currentSortOrder = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[1] === 'asc' ? 1 : -1
})

async function loadDashboard() {
  dashboardLoading.value = true
  dashboardError.value = null
  try {
    dashboard.value = await getAdminDashboard()
  } catch (e: any) {
    dashboardError.value = e.message || t('admin.home.error')
  } finally {
    dashboardLoading.value = false
  }
}

async function loadResumes() {
  tableLoading.value = true
  tableError.value = null
  try {
    const data = await getAdminResumes({
      page: queryParams.page,
      size: queryParams.size,
      search: queryParams.search,
      language: queryParams.language,
      adaptationLevel: queryParams.adaptationLevel,
      createdFrom: queryParams.dateFrom,
      createdTo: queryParams.dateTo,
      sort: queryParams.sort
    })
    resumes.value = data.items
    totalRecords.value = data.totalElements
  } catch (e: any) {
    tableError.value = e.message || t('admin.home.error')
    resumes.value = []
    totalRecords.value = 0
  } finally {
    tableLoading.value = false
  }
}

function scrollToResumes() {
  if (resumesSection.value) {
    resumesSection.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// --- Table events ---
function onPage(event: any) {
  queryParams.page = Math.floor(event.first / event.rows)
  queryParams.size = event.rows
  loadResumes()
}

const ALLOWED_SORT_FIELDS = new Set([
  'resumeTitle', 'vacancyTitle', 'companyName',
  'language', 'adaptationLevel', 'createdAt',
  'ownerUsername', 'ownerEmail', 'ownerFullName'
])

function onSort(event: any) {
  const rawField = event.sortField || 'createdAt'
  // Only use field if it is in the whitelist; otherwise fall back to default
  const field = ALLOWED_SORT_FIELDS.has(rawField) ? rawField : 'createdAt'
  const order = event.sortOrder === -1 ? 'desc' : 'asc'
  queryParams.sort = `${field},${order}`
  queryParams.page = 0
  loadResumes()
}

function onFilter(filters: any) {
  queryParams.language = filters.language || undefined
  queryParams.adaptationLevel = filters.adaptationLevel || undefined
  queryParams.dateFrom = filters.dateFrom || undefined
  queryParams.dateTo = filters.dateTo || undefined
  queryParams.page = 0
  loadResumes()
}

function onSearch(search: string) {
  queryParams.search = search || undefined
  queryParams.page = 0
  loadResumes()
}

function openResumeModal(resume: AdminSavedResume) {
  selectedResume.value = resume
  modalVisible.value = true
}

async function handleDelete(resumeId: number) {
  if (deleteLoading.value) return
  deleteLoading.value = true
  try {
    await deleteAdminResume(resumeId)
    toast.add({ severity: 'success', summary: '', detail: t('admin.resumeDetails.deleteSuccess'), life: 3000 })
    modalVisible.value = false
    selectedResume.value = null
    await Promise.all([loadDashboard(), loadResumes()])
  } catch (e: any) {
    toast.add({ severity: 'error', summary: '', detail: t('admin.resumeDetails.deleteFailed'), life: 3000 })
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  loadDashboard()
  loadResumes()
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
.page-h1 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10233F;
  margin: 0;
}
.section-header {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.section-header h2 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.15rem;
  font-weight: 700;
  color: #10233F;
  margin: 0;
}
.skeleton-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.stats-skeleton-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
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
@media (max-width: 639px) {
  .stats-skeleton-row {
    grid-template-columns: 1fr;
  }
}
</style>
