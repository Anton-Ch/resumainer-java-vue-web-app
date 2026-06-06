import { ref, reactive } from 'vue'
import { fetchSummary } from '@/services/userHomeService'
import { fetchResumes, deleteResume as apiDeleteResume } from '@/services/resumeService'
import type { UserHomeSummary, SavedResumeData } from '@/services/userHomeService'
import type { ResumeQueryParams, PagedResponse } from '@/services/resumeService'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'

export function useUserHome() {
  const toast = useToast()
  const { t } = useI18n()

  // --- Summary state ---
  const summary = ref<UserHomeSummary | null>(null)
  const summaryLoading = ref(true)
  const summaryError = ref<string | null>(null)

  // --- Table state ---
  const resumes = ref<SavedResumeData[]>([])
  const totalRecords = ref(0)
  const tableLoading = ref(true)
  const tableError = ref<string | null>(null)

  // --- Query params for lazy DataTable ---
  const queryParams = reactive<ResumeQueryParams>({
    page: 0,
    size: 10,
    sort: 'createdAt,desc'
  })

  // --- Selected resume for modal ---
  const selectedResume = ref<SavedResumeData | null>(null)
  const modalVisible = ref(false)

  /**
   * Fetch both summary and resumes independently (FR-046).
   */
  async function fetchAll() {
    await Promise.all([loadSummary(), loadResumes()])
  }

  /**
   * Load profile summary (independent block, FR-046).
   */
  async function loadSummary() {
    summaryLoading.value = true
    summaryError.value = null
    try {
      summary.value = await fetchSummary()
    } catch (e: any) {
      summaryError.value = e.message || 'Failed to load summary'
    } finally {
      summaryLoading.value = false
    }
  }

  /**
   * Load resumes page with current query params.
   * Emits events to parent for lazy DataTable integration.
   */
  async function loadResumes() {
    tableLoading.value = true
    tableError.value = null
    try {
      const data: PagedResponse = await fetchResumes(queryParams)
      resumes.value = data.items
      totalRecords.value = data.totalElements
    } catch (e: any) {
      tableError.value = e.message || 'Failed to load resumes'
      resumes.value = []
      totalRecords.value = 0
    } finally {
      tableLoading.value = false
    }
  }

  /**
   * Refresh both blocks (called after delete).
   */
  async function refresh() {
    await Promise.all([loadSummary(), loadResumes()])
  }

  /**
   * Handle DataTable lazy events: page, sort, filter.
   */
  function onPage(event: any) {
    queryParams.page = Math.floor(event.first / event.rows)
    queryParams.size = event.rows
    loadResumes()
  }

  function onSort(event: any) {
    const field = event.sortField || 'createdAt'
    const order = event.sortOrder === -1 ? 'desc' : 'asc'
    queryParams.sort = `${field},${order}`
    queryParams.page = 0
    loadResumes()
  }

  function onFilter(filters: any) {
    queryParams.language = filters.language?.length ? filters.language.join(',') : undefined
    queryParams.adaptationLevel = filters.adaptationLevel?.length ? filters.adaptationLevel.join(',') : undefined
    queryParams.createdDate = filters.createdDate || undefined
    queryParams.page = 0
    loadResumes()
  }

  function onSearch(search: string) {
    queryParams.search = search || undefined
    queryParams.page = 0
    loadResumes()
  }

  /**
   * Open Resume Details modal.
   */
  function openResumeModal(resume: SavedResumeData) {
    selectedResume.value = resume
    modalVisible.value = true
  }

  function closeModal() {
    modalVisible.value = false
    selectedResume.value = null
  }

  /**
   * Delete a resume, refresh both blocks, show toast.
   */
  async function handleDelete(resumeId: number) {
    try {
      await apiDeleteResume(resumeId)
      toast.add({ severity: 'success', summary: '', detail: t('deleteResume.success'), life: 3000 })
      closeModal()
      await refresh()
    } catch (e: any) {
      toast.add({ severity: 'error', summary: '', detail: e.message || 'Delete failed', life: 3000 })
    }
  }

  return {
    summary,
    summaryLoading,
    summaryError,
    resumes,
    totalRecords,
    tableLoading,
    tableError,
    queryParams,
    selectedResume,
    modalVisible,
    fetchAll,
    loadSummary,
    loadResumes,
    refresh,
    onPage,
    onSort,
    onFilter,
    onSearch,
    openResumeModal,
    closeModal,
    handleDelete
  }
}
