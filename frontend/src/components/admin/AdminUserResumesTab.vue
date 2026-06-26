<template>
  <div class="resumes-tab">
    <div class="tab-header">
      <h3>{{ $t('admin.userDetails.userResumesTitle') }}</h3>
      <p>{{ $t('admin.userDetails.userResumesDescription') }}</p>
    </div>

    <div v-if="error" class="inline-error">
      <i class="pi pi-exclamation-triangle"></i>
      <span>{{ error }}</span>
      <Button icon="pi pi-refresh" :label="$t('admin.userDetails.retry')" class="p-button-text p-button-sm" @click="loadResumes" />
    </div>

    <div v-if="loading && resumes.length === 0" class="skeleton-block">
      <Skeleton width="100%" height="200px" />
    </div>

    <AdminResumesTable
      v-else
      :items="resumes"
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
      @openResume="openResumeModal"
    />

    <AdminResumeDetailsDialog
      v-model:visible="modalVisible"
      :resume="selectedResume"
      :delete-loading="deleteLoading"
      @delete="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import AdminResumesTable from '@/components/admin/AdminResumesTable.vue'
import AdminResumeDetailsDialog from '@/components/admin/AdminResumeDetailsDialog.vue'
import { getAdminUserResumes, deleteAdminResume } from '@/services/adminService'
import type { AdminSavedResume } from '@/types/admin'

const props = defineProps<{
  userId: string
}>()

const toast = useToast()
const { t } = useI18n()

const resumes = ref<AdminSavedResume[]>([])
const totalRecords = ref(0)
const loading = ref(true)
const error = ref<string | null>(null)

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

const selectedResume = ref<AdminSavedResume | null>(null)
const modalVisible = ref(false)
const deleteLoading = ref(false)

const firstRow = computed(() => (queryParams.page || 0) * (queryParams.size || 10))

const currentSortField = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[0] || 'createdAt'
})

const currentSortOrder = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[1] === 'asc' ? 1 : -1
})

const ALLOWED_SORT_FIELDS = new Set([
  'resumeTitle', 'vacancyTitle', 'companyName',
  'language', 'adaptationLevel', 'createdAt',
  'ownerUsername', 'ownerEmail', 'ownerFullName'
])

async function loadResumes() {
  loading.value = true
  error.value = null
  try {
    const data = await getAdminUserResumes(props.userId, {
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
    error.value = e.message || t('admin.userDetails.userResumesLoadFailed')
    resumes.value = []
    totalRecords.value = 0
  } finally {
    loading.value = false
  }
}

function onPage(event: any) {
  queryParams.page = Math.floor(event.first / event.rows)
  queryParams.size = event.rows
  loadResumes()
}

function onSort(event: any) {
  const rawField = event.sortField || 'createdAt'
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
    await loadResumes()
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('admin.resumeDetails.deleteFailed'), life: 3000 })
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  loadResumes()
})
</script>

<style scoped>
.resumes-tab {
  width: 100%;
}
.tab-header {
  margin-bottom: 1rem;
}
.tab-header h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.tab-header p {
  margin: 0;
  color: #5D718B;
  font-size: 0.85rem;
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
.skeleton-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
</style>
