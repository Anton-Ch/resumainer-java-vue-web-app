<template>
  <div ref="sectionTopRef">
    <ProfileSectionHeader
      :title="$t('profile.projects.title')"
      :purpose="$t('profile.projects.purpose')"
    />

    <EmptyRecordsState
      v-if="records.length === 0 && !formVisible"
      :title="$t('profile.projects.emptyTitle')"
      :hint="$t('profile.projects.emptyHint')"
    />

    <Button
      v-if="!formVisible"
      :label="$t('profile.projects.add')"
      icon="pi pi-plus"
      class="p-button-success p-button-outlined add-btn"
      @click="openAddForm"
    />

    <div v-if="formVisible" ref="formRef" class="inline-form">
      <div class="form-header">
        <h4 class="form-title">{{ editingId ? $t('profile.projects.edit') : $t('profile.projects.add') }}</h4>
      </div>
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.projectName') }} <span class="required">*</span></label>
          <InputText v-model="formData.projectName" class="form-input" @blur="validateField('projectName')" :class="{ 'p-invalid': formErrors.projectName }" />
          <small v-if="formErrors.projectName" class="field-error">{{ formErrors.projectName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.role') }}</label>
          <InputText v-model="formData.role" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.location') }} <span class="required">*</span></label>
          <InputText v-model="formData.location" class="form-input" @blur="validateField('location')" :class="{ 'p-invalid': formErrors.location }" />
          <small v-if="formErrors.location" class="field-error">{{ formErrors.location }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.startDate') }} <span class="required">*</span></label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
          <small v-if="formErrors.startDate" class="field-error">{{ formErrors.startDate }}</small>
        </div>
        <div class="form-group" v-if="!formData.isOngoing">
          <label class="form-label">{{ $t('profile.projects.endDate') }}</label>
          <DatePicker v-model="formData.endDate" class="form-input" :showIcon="true" :minDate="formData.startDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group form-group-checkbox field-full">
          <Checkbox v-model="formData.isOngoing" :binary="true" inputId="proj-ongoing" />
          <label for="proj-ongoing">{{ $t('profile.projects.isOngoing') }}</label>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.projects.description') }} <span class="required">*</span></label>
          <Textarea v-model="formData.description" class="form-input" rows="3" @blur="validateField('description')" :class="{ 'p-invalid': formErrors.description }" />
          <small v-if="formErrors.description" class="field-error">{{ formErrors.description }}</small>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.projects.projectUrl') }}</label>
          <InputText v-model="formData.projectUrl" class="form-input" />
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.projects.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
        <Button :label="$t('profile.projects.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="rec.projectName"
      :metaLine="formatMeta(rec)"
      :description="rec.description"
      :url="rec.projectUrl || undefined"
      :chipLabel="rec.isOngoing ? $t('profile.projects.ongoing') : undefined"
      editLabelKey="profile.projects.edit"
      deleteLabelKey="profile.projects.delete"
      @edit="openEditForm(rec)"
      @delete="confirmDelete(rec)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useI18n } from 'vue-i18n'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import { fetchProjects, createProject, updateProject, deleteProject } from '@/services/profileService'
import type { Project } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import RecordCard from '../RecordCard.vue'
import EmptyRecordsState from '../EmptyRecordsState.vue'

const toast = useToast()
const confirm = useConfirm()
const { t, locale } = useI18n()
const saving = ref(false)

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<Project[]>([])
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const dateError = ref('')

const formData = reactive({
  projectName: '',
  role: '',
  location: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  isOngoing: false,
  description: '',
  projectUrl: ''
})

const formErrors = reactive<Record<string, string>>({
  projectName: '',
  startDate: '',
  description: '',
  location: ''
})

function validateField(field: string) {
  const val = formData[field as keyof typeof formData]
  const errKey = field as keyof typeof formErrors
  formErrors[errKey] = ''
  if (!val || (typeof val === 'string' && !val.trim())) {
    formErrors[errKey] = t('profile.contact.fieldRequired')
  }
}

function validateAll(): boolean {
  let valid = true
  const fields = ['projectName', 'startDate', 'description', 'location'] as const
  for (const f of fields) {
    validateField(f)
    if (formErrors[f]) valid = false
  }
  return valid
}

function validateDates(): boolean {
  dateError.value = ''
  if (formData.startDate && formData.endDate && !formData.isOngoing) {
    if (formData.endDate < formData.startDate) {
      dateError.value = t('profile.dateRangeError')
      return false
    }
  }
  return true
}

function resetForm() {
  formData.projectName = ''
  formData.role = ''
  formData.location = ''
  formData.startDate = null
  formData.endDate = null
  formData.isOngoing = false
  formData.description = ''
  formData.projectUrl = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

async function loadRecords() {
  try {
    records.value = await fetchProjects()
  } catch {
    records.value = []
  }
}

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function formatMeta(rec: Project): string {
  const parts: string[] = []
  if (rec.role) parts.push(rec.role)
  const start = formatDate(rec.startDate)
  const end = rec.isOngoing ? t('profile.projects.present') : formatDate(rec.endDate)
  if (start || end) parts.push(start + ' — ' + end)
  return parts.join(' · ')
}

function scrollToSectionTitle() {
  nextTick(() => {
    sectionTopRef.value?.querySelector('.section-title-area')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function openAddForm() {
  resetForm()
  formVisible.value = true
  scrollToSectionTitle()
}

function openEditForm(rec: Project) {
  formData.projectName = rec.projectName
  formData.role = rec.role
  formData.location = rec.location
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.isOngoing = rec.isOngoing
  formData.description = rec.description
  formData.projectUrl = rec.projectUrl
  editingId.value = rec.id
  formVisible.value = true
  scrollToSectionTitle()
}

function closeForm() {
  formVisible.value = false
  resetForm()
  emit('dirtyChange', false)
}

function toISODate(d: Date | null): string | null {
  if (!d) return null
  return d.toISOString().split('T')[0]
}

async function handleSave() {
  if (!validateDates()) return
  if (!validateAll()) return
  saving.value = true
  try {
    const payload = {
      projectName: formData.projectName,
      role: formData.role,
      location: formData.location,
      startDate: toISODate(formData.startDate) || '',
      endDate: formData.isOngoing ? null : toISODate(formData.endDate),
      isOngoing: formData.isOngoing,
      description: formData.description,
      projectUrl: formData.projectUrl
    }

    if (editingId.value !== null) {
      await updateProject(editingId.value, { ...payload, id: editingId.value })
    } else {
      await createProject(payload)
    }

    await loadRecords()
    closeForm()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  } finally {
    saving.value = false
  }
}

function confirmDelete(rec: Project) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: async () => {
      try {
        await deleteProject(rec.id)
        await loadRecords()
        emit('saved')
        emit('dirtyChange', false)
        toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
      } catch {
        toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
      }
    }
  })
}

watch(formVisible, (visible) => {
  emit('dirtyChange', visible)
})

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.add-btn {
  margin-bottom: 16px;
}
.inline-form {
  background: var(--vue-bg-subtle);
  border: 1px solid var(--vue-border-default);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
  margin-bottom: 16px;
}
.form-header {
  margin-bottom: 14px;
}
.form-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
}
.form-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-group-checkbox {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
.form-group-checkbox label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-primary);
  cursor: pointer;
}
.field-full {
  grid-column: 1 / -1;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.form-input {
  width: 100%;
}
.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-fields {
    grid-template-columns: 1fr;
  }
}
</style>
