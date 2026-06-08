<template>
  <div ref="sectionTopRef">
    <ProfileSectionHeader
      :title="$t('profile.experience.title')"
      :purpose="$t('profile.experience.purpose')"
    />

    <EmptyRecordsState
      v-if="records.length === 0 && !formVisible"
      :title="$t('profile.experience.emptyTitle')"
      :hint="$t('profile.experience.emptyHint')"
    />

    <Button
      v-if="!formVisible"
      :label="$t('profile.experience.add')"
      icon="pi pi-plus"
      class="p-button-success p-button-outlined add-btn"
      @click="openAddForm"
    />

    <div v-if="formVisible" ref="formRef" class="inline-form">
      <div class="form-header">
        <h4 class="form-title">{{ editingId ? $t('profile.experience.edit') : $t('profile.experience.add') }}</h4>
      </div>
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.jobTitle') }} <span class="required">*</span></label>
          <InputText v-model="formData.jobTitle" class="form-input" @blur="validateField('jobTitle')" :class="{ 'p-invalid': formErrors.jobTitle }" />
          <small v-if="formErrors.jobTitle" class="field-error">{{ formErrors.jobTitle }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.companyName') }} <span class="required">*</span></label>
          <InputText v-model="formData.companyName" class="form-input" @blur="validateField('companyName')" :class="{ 'p-invalid': formErrors.companyName }" />
          <small v-if="formErrors.companyName" class="field-error">{{ formErrors.companyName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.location') }} <span class="required">*</span></label>
          <InputText v-model="formData.location" class="form-input" :placeholder="$t('profile.experience.locationPlaceholder')" @blur="validateField('location')" :class="{ 'p-invalid': formErrors.location }" />
          <small v-if="formErrors.location" class="field-error">{{ formErrors.location }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.companyUrl') }}</label>
          <InputText v-model="formData.companyUrl" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.startDate') }} <span class="required">*</span></label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
          <small v-if="formErrors.startDate" class="field-error">{{ formErrors.startDate }}</small>
        </div>
        <div class="form-group" v-if="!formData.currentlyWorkHere">
          <label class="form-label">{{ $t('profile.experience.endDate') }}</label>
          <DatePicker v-model="formData.endDate" class="form-input" :showIcon="true" :minDate="formData.startDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group form-group-checkbox field-full">
          <Checkbox v-model="formData.currentlyWorkHere" :binary="true" inputId="we-current" />
          <label for="we-current">{{ $t('profile.experience.currentlyWorkHere') }}</label>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.experience.description') }} <span class="required">*</span></label>
          <Textarea v-model="formData.description" class="form-input" rows="3" @blur="validateField('description')" :class="{ 'p-invalid': formErrors.description }" />
          <small v-if="formErrors.description" class="field-error">{{ formErrors.description }}</small>
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.experience.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
        <Button :label="$t('profile.experience.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="rec.jobTitle"
      :metaLine="formatMeta(rec)"
      :description="rec.description"
      :url="rec.companyUrl || undefined"
      :chipLabel="rec.currentlyWorkHere ? $t('profile.experience.current') : undefined"
      editLabelKey="profile.experience.edit"
      deleteLabelKey="profile.experience.delete"
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
import { fetchExperiences, createExperience, updateExperience, deleteExperience } from '@/services/profileService'
import type { WorkExperience } from '@/types/profile'
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

const records = ref<WorkExperience[]>([])
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const dateError = ref('')

const formData = reactive({
  jobTitle: '',
  companyName: '',
  location: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  currentlyWorkHere: false,
  description: '',
  companyUrl: ''
})

const formErrors = reactive<Record<string, string>>({
  jobTitle: '',
  companyName: '',
  location: '',
  startDate: '',
  description: ''
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
  const fields = ['jobTitle', 'companyName', 'location', 'startDate', 'description'] as const
  for (const f of fields) {
    validateField(f)
    if (formErrors[f]) valid = false
  }
  return valid
}

function validateDates(): boolean {
  dateError.value = ''
  if (formData.startDate && formData.endDate && !formData.currentlyWorkHere) {
    if (formData.endDate < formData.startDate) {
      dateError.value = t('profile.dateRangeError')
      return false
    }
  }
  return true
}

function resetForm() {
  formData.jobTitle = ''
  formData.companyName = ''
  formData.location = ''
  formData.startDate = null
  formData.endDate = null
  formData.currentlyWorkHere = false
  formData.description = ''
  formData.companyUrl = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

async function loadRecords() {
  try {
    records.value = await fetchExperiences()
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

function formatMeta(rec: WorkExperience): string {
  const parts = [rec.companyName]
  const start = formatDate(rec.startDate)
  const end = rec.currentlyWorkHere ? t('profile.experience.present') : formatDate(rec.endDate)
  if (start || end) parts.push(start + ' — ' + end)
  if (rec.location) parts.push(rec.location)
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

function openEditForm(rec: WorkExperience) {
  formData.jobTitle = rec.jobTitle
  formData.companyName = rec.companyName
  formData.location = rec.location
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.currentlyWorkHere = rec.currentlyWorkHere
  formData.description = rec.description
  formData.companyUrl = rec.companyUrl
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
      jobTitle: formData.jobTitle,
      companyName: formData.companyName,
      location: formData.location,
      startDate: toISODate(formData.startDate) || '',
      endDate: formData.currentlyWorkHere ? null : toISODate(formData.endDate),
      currentlyWorkHere: formData.currentlyWorkHere,
      description: formData.description,
      companyUrl: formData.companyUrl
    }

    if (editingId.value !== null) {
      await updateExperience(editingId.value, { ...payload, id: editingId.value })
    } else {
      await createExperience(payload)
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

function confirmDelete(rec: WorkExperience) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: async () => {
      try {
        await deleteExperience(rec.id)
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
