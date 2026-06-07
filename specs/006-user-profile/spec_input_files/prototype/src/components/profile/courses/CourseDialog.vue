<template>
  <Dialog
    v-model:visible="localVisible"
    modal
    :header="dialogTitle"
    :style="{ maxWidth: '560px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
    @hide="onClose"
  >
    <template v-if="mode === 'view' && course">
      <div class="detail-grid">
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.courseName') }}</div>
          <div class="detail-value">{{ course.courseName }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.provider') }}</div>
          <div class="detail-value">{{ course.provider }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.startDate') }}</div>
          <div class="detail-value">{{ formatDate(course.startDate) }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.endDate') }}</div>
          <div class="detail-value">{{ formatDate(course.endDate) }}</div>
        </div>
        <div class="detail-field" v-if="course.credentialUrl">
          <div class="detail-label">{{ $t('profile.courses.credentialUrl') }}</div>
          <div class="detail-value"><a :href="course.credentialUrl" target="_blank">{{ course.credentialUrl }}</a></div>
        </div>
        <div class="detail-field-full" v-if="course.skills">
          <div class="detail-label">{{ $t('profile.courses.skills') }}</div>
          <div class="detail-value">{{ course.skills }}</div>
        </div>
        <div class="detail-field-full" v-if="course.description">
          <div class="detail-label">{{ $t('profile.courses.description') }}</div>
          <div class="detail-value">{{ course.description }}</div>
        </div>
      </div>
      <div class="dialog-actions">
        <Button :label="$t('profile.courses.edit')" icon="pi pi-pencil" class="p-button-success p-button-outlined" @click="switchToEdit" />
        <Button :label="$t('profile.courses.delete')" icon="pi pi-trash" class="p-button-danger" style="margin-left: auto;" @click="confirmDelete" />
      </div>
    </template>

    <template v-else-if="mode !== 'view'">
      <div class="edit-form">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.courseName') }} <span class="required">*</span></label>
          <InputText v-model="editForm.courseName" class="form-input" />
          <small v-if="errors.courseName" class="field-error">{{ errors.courseName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.provider') }} <span class="required">*</span></label>
          <InputText v-model="editForm.provider" class="form-input" />
          <small v-if="errors.provider" class="field-error">{{ errors.provider }}</small>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.courses.startDate') }} <span class="required">*</span></label>
            <DatePicker v-model="editForm.startDate" class="form-input" :showIcon="true" :maxDate="editForm.endDate || undefined" />
            <small v-if="errors.startDate" class="field-error">{{ errors.startDate }}</small>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.courses.endDate') }}</label>
            <DatePicker v-model="editForm.endDate" class="form-input" :showIcon="true" :minDate="editForm.startDate || undefined" />
            <small v-if="errors.endDate" class="field-error">{{ errors.endDate }}</small>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.credentialUrl') }}</label>
          <InputText v-model="editForm.credentialUrl" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.skills') }}</label>
          <InputText v-model="editForm.skills" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.description') }}</label>
          <Textarea v-model="editForm.description" class="form-input" rows="3" />
        </div>
      </div>
    </template>

    <template #footer>
      <div v-if="mode !== 'view'" class="dialog-actions">
        <p class="required-hint" style="margin:0 auto 0 0">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.courses.cancel')" class="p-button-text" @click="onClose" />
        <Button :label="$t('profile.courses.save')" icon="pi pi-check" class="p-button-success" @click="onSave" />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useConfirm } from 'primevue/useconfirm'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import type { Course } from '@/types/profile'

const props = defineProps<{
  visible: boolean
  course: Course | null
  mode: 'view' | 'add' | 'edit'
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  save: [course: Course]
  delete: [id: string]
}>()

const { t, locale } = useI18n()
const confirm = useConfirm()

const localVisible = ref(props.visible)

watch(() => props.visible, (v) => { localVisible.value = v })
watch(localVisible, (v) => { emit('update:visible', v) })

const editForm = reactive({
  courseName: '',
  provider: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  credentialUrl: '',
  skills: '',
  description: ''
})

const errors = reactive({
  courseName: '',
  provider: '',
  startDate: '',
  endDate: ''
})

function resetEditForm() {
  editForm.courseName = ''
  editForm.provider = ''
  editForm.startDate = null
  editForm.endDate = null
  editForm.credentialUrl = ''
  editForm.skills = ''
  editForm.description = ''
  errors.courseName = ''
  errors.provider = ''
  errors.startDate = ''
  errors.endDate = ''
}

watch(() => props.course, (course) => {
  if (course && props.mode === 'edit') {
    editForm.courseName = course.courseName
    editForm.provider = course.provider
    editForm.startDate = course.startDate ? new Date(course.startDate) : null
    editForm.endDate = course.endDate ? new Date(course.endDate) : null
    editForm.credentialUrl = course.credentialUrl
    editForm.skills = course.skills
    editForm.description = course.description
  } else if (props.mode === 'add') {
    resetEditForm()
  }
})

const dialogTitle = computed(() => {
  if (props.mode === 'add') return t('profile.courses.addTitle')
  if (props.mode === 'edit') return t('profile.courses.editTitle')
  return t('profile.courses.detailsTitle')
})

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function switchToEdit() {
  if (props.course) {
    editForm.courseName = props.course.courseName
    editForm.provider = props.course.provider
    editForm.startDate = props.course.startDate ? new Date(props.course.startDate) : null
    editForm.endDate = props.course.endDate ? new Date(props.course.endDate) : null
    editForm.credentialUrl = props.course.credentialUrl
    editForm.skills = props.course.skills
    editForm.description = props.course.description
    emit('update:visible', true)
  }
}

function validate(): boolean {
  let valid = true
  errors.courseName = ''
  errors.provider = ''
  errors.startDate = ''
  errors.endDate = ''

  if (!editForm.courseName) { errors.courseName = t('profile.contact.fieldRequired'); valid = false }
  if (!editForm.provider) { errors.provider = t('profile.contact.fieldRequired'); valid = false }
  if (!editForm.startDate) { errors.startDate = t('profile.contact.fieldRequired'); valid = false }

  if (editForm.startDate && editForm.endDate && editForm.endDate < editForm.startDate) {
    errors.endDate = t('profile.dateRangeError')
    valid = false
  }

  return valid
}

function onSave() {
  if (!validate()) return

  const course: Course = {
    id: props.course?.id || '',
    courseName: editForm.courseName,
    provider: editForm.provider,
    startDate: editForm.startDate ? editForm.startDate.toISOString() : '',
    endDate: editForm.endDate ? editForm.endDate.toISOString() : '',
    credentialUrl: editForm.credentialUrl,
    skills: editForm.skills,
    description: editForm.description
  }
  emit('save', course)
  localVisible.value = false
}

function onClose() {
  localVisible.value = false
}

function confirmDelete() {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: () => {
      if (props.course) emit('delete', props.course.id)
      localVisible.value = false
    }
  })
}
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
}
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.detail-field, .detail-field-full {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.detail-label {
  font-size: var(--vue-text-xs);
  font-weight: 600;
  color: var(--vue-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.detail-value {
  font-size: var(--vue-text-base);
  color: var(--vue-text-primary);
}
.detail-value a {
  color: var(--vue-accent-blue);
  word-break: break-all;
}
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
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
.form-input {
  width: 100%;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
}
.dialog-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
</style>
