<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.courses.title')"
      :purpose="$t('profile.courses.purpose')"
    />

    <CoursesTable
      :data="records"
      :totalRecords="totalRecords"
      :loading="loading"
      @add="openAddDialog"
      @open="openViewDialog"
      @update:params="onParamsChange"
    />

    <CourseDialog
      v-model:visible="dialogVisible"
      :course="selectedCourse"
      :mode="dialogMode"
      @save="handleSave"
      @delete="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import { fetchCourses, createCourse, updateCourse, deleteCourse } from '@/services/profileService'
import type { Course } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import CoursesTable from '../courses/CoursesTable.vue'
import CourseDialog from '../courses/CourseDialog.vue'

const toast = useToast()
const { t } = useI18n()

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<Course[]>([])
const totalRecords = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const selectedCourse = ref<Course | null>(null)
const dialogMode = ref<'view' | 'add' | 'edit'>('view')

const queryParams = ref({
  page: 0,
  size: 10,
  sort: 'start_date',
  order: 'desc',
  search: '',
  dateFrom: '',
  dateTo: ''
})

async function loadRecords() {
  loading.value = true
  try {
    const page = await fetchCourses({
      page: queryParams.value.page,
      size: queryParams.value.size,
      sort: queryParams.value.sort,
      order: queryParams.value.order,
      search: queryParams.value.search || undefined,
      dateFrom: queryParams.value.dateFrom || undefined,
      dateTo: queryParams.value.dateTo || undefined
    })
    records.value = page.content
    totalRecords.value = page.totalElements
  } catch {
    records.value = []
    totalRecords.value = 0
  } finally {
    loading.value = false
  }
}

function onParamsChange(params: typeof queryParams.value) {
  queryParams.value = { ...params }
  loadRecords()
}

function openAddDialog() {
  selectedCourse.value = null
  dialogMode.value = 'add'
  dialogVisible.value = true
}

function openViewDialog(course: Course) {
  selectedCourse.value = course
  dialogMode.value = 'view'
  dialogVisible.value = true
}

async function handleSave(course: Course) {
  try {
    if (course.id) {
      await updateCourse(course.id, course)
    } else {
      await createCourse(course)
    }
    await loadRecords()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

async function handleDelete(id: number) {
  try {
    await deleteCourse(id)
    await loadRecords()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>
