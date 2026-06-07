import { ref } from 'vue'

export function useUserHome() {
  const summary = ref(null)
  const summaryLoading = ref(false)
  const summaryError = ref('')
  const resumes = ref([])
  const totalRecords = ref(0)
  const tableLoading = ref(false)
  const tableError = ref('')
  const queryParams = ref({ page: 0, size: 10, sort: 'createdAt,desc' })
  const selectedResume = ref(null)
  const modalVisible = ref(false)

  async function fetchAll() {}
  async function loadSummary() {}
  async function loadResumes() {}
  function onPage(e: any) {}
  function onSort(e: any) {}
  function onFilter(e: any) {}
  function onSearch(q: string) {}
  function openResumeModal(r: any) {
    selectedResume.value = r
    modalVisible.value = true
  }
  function closeModal() { modalVisible.value = false }
  async function handleDelete(id: number) {}

  return {
    summary, summaryLoading, summaryError,
    resumes, totalRecords, tableLoading, tableError,
    queryParams, selectedResume, modalVisible,
    fetchAll, loadSummary, loadResumes,
    onPage, onSort, onFilter, onSearch,
    openResumeModal, closeModal, handleDelete
  }
}
