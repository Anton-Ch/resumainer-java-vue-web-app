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
        <!-- Bilingual fields: RU (left) / EN (right) -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.education.institutionNameRu') }} <span class="required">*</span></label>
            <InputText v-model="formData.institutionNameRu" class="form-input" @blur="validateField('institutionNameRu')" :class="{ 'p-invalid': formErrors.institutionNameRu }" />
            <small v-if="formErrors.institutionNameRu" class="field-error">{{ formErrors.institutionNameRu }}</small>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.education.institutionNameEn') }} <span class="required">*</span></label>
            <InputText v-model="formData.institutionNameEn" class="form-input" @blur="validateField('institutionNameEn')" :class="{ 'p-invalid': formErrors.institutionNameEn }" />
            <small v-if="formErrors.institutionNameEn" class="field-error">{{ formErrors.institutionNameEn }}</small>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.education.degreeRu') }} <span class="required">*</span></label>
            <InputText v-model="formData.degreeRu" class="form-input" @blur="validateField('degreeRu')" :class="{ 'p-invalid': formErrors.degreeRu }" />
            <small v-if="formErrors.degreeRu" class="field-error">{{ formErrors.degreeRu }}</small>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.education.degreeEn') }} <span class="required">*</span></label>
            <InputText v-model="formData.degreeEn" class="form-input" @blur="validateField('degreeEn')" :class="{ 'p-invalid': formErrors.degreeEn }" />
            <small v-if="formErrors.degreeEn" class="field-error">{{ formErrors.degreeEn }}</small>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.education.fieldOfStudyRu') }} <span class="required">*</span></label>
            <InputText v-model="formData.fieldOfStudyRu" class="form-input" @blur="validateField('fieldOfStudyRu')" :class="{ 'p-invalid': formErrors.fieldOfStudyRu }" />
            <small v-if="formErrors.fieldOfStudyRu" class="field-error">{{ formErrors.fieldOfStudyRu }}</small>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.education.fieldOfStudyEn') }} <span class="required">*</span></label>
            <InputText v-model="formData.fieldOfStudyEn" class="form-input" @blur="validateField('fieldOfStudyEn')" :class="{ 'p-invalid': formErrors.fieldOfStudyEn }" />
            <small v-if="formErrors.fieldOfStudyEn" class="field-error">{{ formErrors.fieldOfStudyEn }}</small>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.gpa') }}</label>
          <InputText v-model="formData.gpaGrade" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.startDate') }} <span class="required">*</span></label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate && formData.endDate < new Date() ? formData.endDate : new Date()" />
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
          <Textarea v-model="formData.description" class="form-input" rows="2" />
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.education.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
        <Button :label="$t('profile.education.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="educationTitle(rec)"
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
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useI18n } from 'vue-i18n'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import { fetchEducations, createEducation, updateEducation, deleteEducation } from '@/services/profileService'
import type { Education } from '@/types/profile'
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

const records = ref<Education[]>([])
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const dateError = ref('')

const formData = reactive({
  institutionNameRu: '',
  institutionNameEn: '',
  degreeRu: '',
  degreeEn: '',
  fieldOfStudyRu: '',
  fieldOfStudyEn: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  currentlyStudying: false,
  location: '',
  description: '',
  gpaGrade: ''
})

const formErrors = reactive<Record<string, string>>({
  institutionNameRu: '',
  institutionNameEn: '',
  degreeRu: '',
  degreeEn: '',
  fieldOfStudyRu: '',
  fieldOfStudyEn: '',
  startDate: ''
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
  const fields = ['institutionNameRu', 'institutionNameEn', 'degreeRu', 'degreeEn', 'fieldOfStudyRu', 'fieldOfStudyEn', 'startDate'] as const
  for (const f of fields) {
    validateField(f)
    if (formErrors[f]) valid = false
  }
  return valid
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

function resetForm() {
  formData.institutionNameRu = ''
  formData.institutionNameEn = ''
  formData.degreeRu = ''
  formData.degreeEn = ''
  formData.fieldOfStudyRu = ''
  formData.fieldOfStudyEn = ''
  formData.startDate = null
  formData.endDate = null
  formData.currentlyStudying = false
  formData.location = ''
  formData.description = ''
  formData.gpaGrade = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

async function loadRecords() {
  try {
    records.value = await fetchEducations()
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

function formatMeta(rec: Education): string {
  const start = formatDate(rec.startDate)
  let end = ''
  if (rec.currentlyStudying) {
    end = t('profile.education.present')
  } else if (rec.endDate) {
    end = formatDate(rec.endDate)
  } else if (rec.startDate) {
    end = t('profile.education.present')
  }
  const dateStr = start + ' — ' + end
  return (rec.location ? dateStr + ' · ' + rec.location : dateStr)
}

/** Returns the current locale (short code). */
const isRu = computed(() => locale.value.startsWith('ru'))

/** Picks the language-appropriate value: prefers current locale, falls back to the other. */
function pickLocalized(ru?: string | null, en?: string | null): string {
  if (isRu.value) {
    return ru || en || ''
  }
  return en || ru || ''
}

/** Returns the education card title in the current UI language. */
function educationTitle(rec: Education): string {
  return pickLocalized(rec.institutionNameRu, rec.institutionNameEn)
}

function formatDescription(rec: Education): string {
  const degree = pickLocalized(rec.degreeRu, rec.degreeEn)
  const field = pickLocalized(rec.fieldOfStudyRu, rec.fieldOfStudyEn)
  return [degree, field].filter(Boolean).join(' \u00B7 ')
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
  formData.institutionNameRu = rec.institutionNameRu
  formData.institutionNameEn = rec.institutionNameEn
  formData.degreeRu = rec.degreeRu
  formData.degreeEn = rec.degreeEn
  formData.fieldOfStudyRu = rec.fieldOfStudyRu
  formData.fieldOfStudyEn = rec.fieldOfStudyEn
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.currentlyStudying = rec.currentlyStudying
  formData.location = rec.location
  formData.description = rec.description
  formData.gpaGrade = rec.gpaGrade
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
      institutionNameRu: formData.institutionNameRu,
      institutionNameEn: formData.institutionNameEn,
      degreeRu: formData.degreeRu,
      degreeEn: formData.degreeEn,
      fieldOfStudyRu: formData.fieldOfStudyRu,
      fieldOfStudyEn: formData.fieldOfStudyEn,
      startDate: toISODate(formData.startDate) || '',
      endDate: formData.currentlyStudying ? null : toISODate(formData.endDate),
      currentlyStudying: formData.currentlyStudying,
      location: formData.location,
      description: formData.description,
      gpaGrade: formData.gpaGrade
    }

    if (editingId.value !== null) {
      await updateEducation(editingId.value, { ...payload, id: editingId.value })
    } else {
      await createEducation(payload)
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

function confirmDelete(rec: Education) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: async () => {
      try {
        await deleteEducation(rec.id)
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
