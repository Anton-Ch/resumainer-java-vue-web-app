<template>
  <div ref="sectionTopRef">
    <ProfileSectionHeader
      :title="$t('profile.education.title')"
      :purpose="$t('profile.education.purpose')"
    />

    <EmptyRecordsState
      v-if="records.length === 0 && !formVisible"
      :title="$t('profile.education.emptyTitle')"
      :hint="$t('profile.education.emptyHint')"
    />

    <Button
      v-if="!formVisible"
      :label="$t('profile.education.add')"
      icon="pi pi-plus"
      class="p-button-success p-button-outlined add-btn"
      @click="openAddForm"
    />

    <div v-if="formVisible" ref="formRef" class="inline-form">
      <div class="form-header">
        <h4 class="form-title">{{ editingId ? $t('profile.education.edit') : $t('profile.education.add') }}</h4>
      </div>
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.institutionName') }} <span class="required">*</span></label>
          <InputText v-model="formData.institutionName" class="form-input" @blur="validateRequiredField('institutionName')" />
          <small v-if="formErrors.institutionName" class="field-error">{{ formErrors.institutionName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.degree') }} <span class="required">*</span></label>
          <InputText v-model="formData.degree" class="form-input" @blur="validateRequiredField('degree')" />
          <small v-if="formErrors.degree" class="field-error">{{ formErrors.degree }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.fieldOfStudy') }}</label>
          <InputText v-model="formData.fieldOfStudy" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.gpa') }}</label>
          <InputText v-model="formData.gpa" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.startDate') }} <span class="required">*</span></label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
          <small v-if="formErrors.startDate" class="field-error">{{ formErrors.startDate }}</small>
        </div>
        <div class="form-group" v-if="!formData.currentlyStudying">
          <label class="form-label">{{ $t('profile.education.endDate') }}</label>
          <DatePicker v-model="formData.endDate" class="form-input" :showIcon="true" :minDate="formData.startDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group form-group-checkbox field-full">
          <Checkbox v-model="formData.currentlyStudying" :binary="true" inputId="edu-current" />
          <label for="edu-current">{{ $t('profile.education.currentlyStudying') }}</label>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.education.location') }}</label>
          <InputText v-model="formData.location" class="form-input" />
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.education.comment') }}</label>
          <Textarea v-model="formData.comment" class="form-input" rows="2" />
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.education.save')" icon="pi pi-check" class="p-button-success" @click="handleSave" />
        <Button :label="$t('profile.education.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="rec.institutionName"
      :metaLine="formatMeta(rec)"
      :description="formatDescription(rec)"
      :chipLabel="rec.currentlyStudying ? $t('profile.education.current') : undefined"
      editLabelKey="profile.education.edit"
      deleteLabelKey="profile.education.delete"
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
import { getEducation, saveEducationRecord, deleteEducationRecord } from '@/services/profileMockService'
import type { Education } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import RecordCard from '../RecordCard.vue'
import EmptyRecordsState from '../EmptyRecordsState.vue'

const toast = useToast()
const confirm = useConfirm()
const { t, locale } = useI18n()

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<Education[]>([])
const formVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const dateError = ref('')

const formData = reactive({
  institutionName: '',
  degree: '',
  fieldOfStudy: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  currentlyStudying: false,
  location: '',
  comment: '',
  gpa: ''
})

const formErrors = reactive<Record<string, string>>({
  institutionName: '',
  degree: '',
  startDate: ''
})

function validateRequiredField(field: string) {
  const val = formData[field as keyof typeof formData]
  formErrors[field as keyof typeof formErrors] = ''
  if (!val) {
    formErrors[field as keyof typeof formErrors] = t('profile.contact.fieldRequired')
  }
}

function validateRequired(): boolean {
  let valid = true
  const fields: (keyof typeof formErrors)[] = ['institutionName', 'degree', 'startDate']
  for (const f of fields) {
    const val = formData[f as keyof typeof formData] as string | Date | null
    formErrors[f] = val ? '' : t('profile.contact.fieldRequired')
    if (formErrors[f]) valid = false
  }
  return valid
}

function resetForm() {
  formData.institutionName = ''
  formData.degree = ''
  formData.fieldOfStudy = ''
  formData.startDate = null
  formData.endDate = null
  formData.currentlyStudying = false
  formData.location = ''
  formData.comment = ''
  formData.gpa = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

function loadRecords() {
  records.value = getEducation()
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function formatMeta(rec: Education): string {
  const start = formatDate(rec.startDate)
  const end = rec.currentlyStudying ? t('profile.education.present') : formatDate(rec.endDate)
  const dateStr = start + ' — ' + end
  return (rec.location ? dateStr + ' · ' + rec.location : dateStr)
}

function formatDescription(rec: Education): string {
  const parts: string[] = []
  if (rec.degree) parts.push(rec.degree)
  if (rec.fieldOfStudy) parts.push(rec.fieldOfStudy)
  return parts.join('\n')
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

function openEditForm(rec: Education) {
  formData.institutionName = rec.institutionName
  formData.degree = rec.degree
  formData.fieldOfStudy = rec.fieldOfStudy
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.currentlyStudying = rec.currentlyStudying
  formData.location = rec.location
  formData.comment = rec.comment
  formData.gpa = rec.gpa
  editingId.value = rec.id
  formVisible.value = true
  scrollToSectionTitle()
}

function closeForm() {
  formVisible.value = false
  resetForm()
  emit('dirtyChange', false)
}

function collectFormData(): Education {
  return {
    id: editingId.value || '',
    institutionName: formData.institutionName,
    degree: formData.degree,
    fieldOfStudy: formData.fieldOfStudy,
    startDate: formData.startDate ? formData.startDate.toISOString() : '',
    endDate: formData.currentlyStudying ? '' : (formData.endDate ? formData.endDate.toISOString() : ''),
    currentlyStudying: formData.currentlyStudying,
    location: formData.location,
    comment: formData.comment,
    gpa: formData.gpa
  }
}

function validateDates(): boolean {
  dateError.value = ''
  if (formData.startDate && formData.endDate && !formData.currentlyStudying) {
    if (formData.endDate < formData.startDate) {
      dateError.value = t('profile.dateRangeError')
      return false
    }
  }
  return true
}

function handleSave() {
  if (!validateDates()) return
  if (!validateRequired()) return
  try {
    records.value = saveEducationRecord(collectFormData())
    closeForm()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

function confirmDelete(rec: Education) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: () => {
      records.value = deleteEducationRecord(rec.id)
      emit('saved')
      emit('dirtyChange', false)
      toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
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
