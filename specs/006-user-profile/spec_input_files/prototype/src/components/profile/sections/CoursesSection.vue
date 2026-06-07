<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.courses.title')"
      :purpose="$t('profile.courses.purpose')"
    />

    <CoursesTable
      :courses="records"
      @add="openAddDialog"
      @open="openViewDialog"
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
import { ref, watch, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import { getCourses, saveCourse, deleteCourse } from '@/services/profileMockService'
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
const dialogVisible = ref(false)
const selectedCourse = ref<Course | null>(null)
const dialogMode = ref<'view' | 'add' | 'edit'>('view')

function loadRecords() {
  records.value = getCourses()
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

function handleSave(course: Course) {
  try {
    records.value = saveCourse(course)
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

function handleDelete(id: string) {
  records.value = deleteCourse(id)
  emit('saved')
  emit('dirtyChange', false)
  toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
}

watch(dialogVisible, (visible) => {
  if (dialogMode.value !== 'view') {
    emit('dirtyChange', visible)
  }
})

watch(dialogMode, (mode) => {
  if (mode !== 'view' && dialogVisible.value) {
    emit('dirtyChange', true)
  }
})

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>
