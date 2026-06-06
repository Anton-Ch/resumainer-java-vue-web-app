<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <h1 class="page-h1">{{ $t('home.title') }}</h1>

      <!-- Guided block: loading / error / content -->
      <div v-if="summaryLoading" class="skeleton-block">
        <Skeleton width="60%" height="24px" />
        <Skeleton width="100%" height="80px" style="margin-top: 1rem;" />
      </div>
      <div v-else-if="summaryError" class="inline-error">
        <i class="pi pi-exclamation-triangle" style="color: #D97706; font-size: 1.25rem;"></i>
        <span>{{ summaryError }}</span>
        <Button icon="pi pi-refresh" label="Retry" class="p-button-text p-button-sm" @click="loadSummary" />
      </div>
      <GuidedNextStep
        v-else-if="summary"
        :profileReady="summary.profileReady"
        :checklist="summary.profileChecklist"
      />

      <!-- Summary cards -->
      <div v-if="summaryLoading" class="skeleton-block">
        <div class="summary-skeleton-row">
          <Skeleton width="100%" height="100px" v-for="i in 3" :key="i" />
        </div>
      </div>
      <SummaryCards
        v-else-if="summary"
        :savedResumesCount="summary.summary.savedResumesCount"
        :profileReady="summary.profileReady"
        :lastResume="summary.lastResume"
        @openLastResume="openLastResume"
      />

      <!-- Saved Resumes section -->
      <div class="section-header">
        <h2>{{ $t('home.table.title') }}</h2>
        <Button
          v-if="summary?.profileReady"
          :label="$t('home.ready.generate.cta')"
          icon="pi pi-plus"
          class="p-button-success p-button-outlined"
          v-tooltip.top="$t('home.ready.generate.tooltip')"
          @click="$router.push('/generate/vacancy')"
        />
      </div>

      <!-- Table: loading / error / content -->
      <div v-if="tableLoading && resumes.length === 0" class="skeleton-block">
        <Skeleton width="100%" height="200px" />
      </div>
      <div v-else-if="tableError" class="inline-error">
        <i class="pi pi-exclamation-triangle" style="color: #C2410C; font-size: 1.25rem;"></i>
        <span>{{ tableError }}</span>
        <Button icon="pi pi-refresh" label="Retry" class="p-button-text p-button-sm" @click="loadResumes" />
      </div>
      <SavedResumesTable
        v-else
        :resumes="resumes"
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
      <ResumeDetailsDialog
        v-model:visible="modalVisible"
        :resume="selectedResume"
        @delete="handleDelete"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserHome } from '@/composables/useUserHome'
import AppHeader from '@/components/AppHeader.vue'
import GuidedNextStep from '@/components/home/GuidedNextStep.vue'
import SummaryCards from '@/components/home/SummaryCards.vue'
import SavedResumesTable from '@/components/home/SavedResumesTable.vue'
import ResumeDetailsDialog from '@/components/home/ResumeDetailsDialog.vue'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'

const router = useRouter()

const {
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
  onPage,
  onSort,
  onFilter,
  onSearch,
  openResumeModal,
  closeModal,
  handleDelete
} = useUserHome()

const firstRow = computed(() => (queryParams.page || 0) * (queryParams.size || 10))

const currentSortField = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[0] || 'createdAt'
})

const currentSortOrder = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[1] === 'asc' ? 1 : -1
})

function openLastResume() {
  if (summary.value?.lastResume) {
    openResumeModal(summary.value.lastResume)
  }
}

onMounted(() => {
  fetchAll()
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
  align-items: center;
  justify-content: space-between;
  margin-top: 0.5rem;
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
.summary-skeleton-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  .summary-skeleton-row {
    grid-template-columns: 1fr;
  }
}
</style>
