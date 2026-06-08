<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.additional.title')"
    />

    <div class="additional-form">
      <!-- Block 1: Resume & Public Profile Preferences -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block1Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.username') }} <span class="required">*</span></label>
            <InputText v-model="form.username" class="form-input" :placeholder="$t('profile.additional.usernamePlaceholder')" @blur="validateUsername" />
            <small v-if="formErrors.username" class="field-error">{{ formErrors.username }}</small>
            <p class="field-hint">{{ $t('profile.additional.usernameHelp') }}</p>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.defaultResumeLanguage') }}</label>
              <Select v-model="form.defaultResumeLanguage" :options="languageOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.additionalResumeLanguage') }}</label>
              <Select v-model="form.additionalResumeLanguage" :options="languageOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
          </div>
        </div>
      </div>

      <!-- Block 2: Work Preferences -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block2Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.acceptableWorkFormats') }}</label>
            <div class="checkbox-group">
              <div v-for="fmt in workFormatOptions" :key="fmt.value" class="checkbox-item">
                <Checkbox v-model="form.acceptableWorkFormats" :inputId="'wf-' + fmt.value" :value="fmt.value" />
                <label :for="'wf-' + fmt.value">{{ fmt.label }}</label>
              </div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.willingnessToRelocate') }}</label>
              <Select v-model="form.willingnessToRelocate" :options="willingnessOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.willingnessForBusinessTravel') }}</label>
              <Select v-model="form.willingnessForBusinessTravel" :options="willingnessOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
          </div>
        </div>
      </div>

      <!-- Block 3: Professional Info -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block3Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.skills') }}</label>
            <Textarea v-model="form.skills" class="form-input" rows="3" :placeholder="$t('profile.additional.skillsPlaceholder')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.spokenLanguages') }}</label>
            <InputText v-model="form.spokenLanguages" :placeholder="$t('profile.additional.spokenLanguagesPlaceholder')" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.professionalAspirations') }}</label>
            <Textarea v-model="form.professionalAspirations" class="form-input" rows="2" :placeholder="$t('profile.additional.professionalAspirationsPlaceholder')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.achievements') }}</label>
            <Textarea v-model="form.achievements" class="form-input" rows="2" :placeholder="$t('profile.additional.achievementsPlaceholder')" />
          </div>
        </div>
      </div>

      <!-- Block 4: Personal Info -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block4Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.additionalContextForAI') }}</label>
            <Textarea v-model="form.additionalContextForAI" class="form-input" rows="3" :placeholder="$t('profile.additional.additionalContextForAIPlaceholder')" />
            <p class="field-hint">{{ $t('profile.additional.additionalContextHint') }}</p>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.dateOfBirth') }} <span class="required">*</span></label>
              <DatePicker v-model="dateOfBirth" class="form-input" :showIcon="true" :maxDate="new Date()" />
              <small v-if="formErrors.dateOfBirth" class="field-error">{{ formErrors.dateOfBirth }}</small>
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.citizenship') }} <span class="required">*</span></label>
              <InputText v-model="form.citizenship" class="form-input" @blur="validateRequired('citizenship')" />
              <small v-if="formErrors.citizenship" class="field-error">{{ formErrors.citizenship }}</small>
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.additional.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import { fetchAdditionalInfo, updateAdditionalInfo } from '@/services/profileService'
import type { AdditionalInfo } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'

const toast = useToast()
const { t, locale } = useI18n()
const saving = ref(false)

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const languageOptions = computed(() => [
  { value: 1, label: t('profile.additional.languageEnglish') },
  { value: 2, label: t('profile.additional.languageRussian') }
])

const workFormatOptions = computed(() => [
  { value: 'full-time', label: t('profile.additional.fullTime') },
  { value: 'part-time', label: t('profile.additional.partTime') },
  { value: 'rotational_schedule', label: t('profile.additional.rotationalSchedule') },
  { value: 'internship', label: t('profile.additional.internship') },
  { value: 'offline', label: t('profile.additional.offline') },
  { value: 'remote', label: t('profile.additional.remote') },
  { value: 'hybrid', label: t('profile.additional.hybrid') },
  { value: 'on_project_site', label: t('profile.additional.onProjectSite') }
])

const willingnessOptions = computed(() => [
  { value: 'Yes', label: t('profile.additional.yes') },
  { value: 'No', label: t('profile.additional.no') },
  { value: 'Negotiable', label: t('profile.additional.negotiable') }
])

const form = reactive<AdditionalInfo>({
  username: '',
  defaultResumeLanguage: null,
  additionalResumeLanguage: null,
  acceptableWorkFormats: [],
  willingnessToRelocate: '',
  willingnessForBusinessTravel: '',
  skills: '',
  spokenLanguages: '',
  professionalAspirations: '',
  achievements: '',
  additionalContextForAI: '',
  dateOfBirth: '',
  citizenship: ''
})

const dateOfBirth = ref<Date | null>(null)

const formErrors = reactive<Record<string, string>>({
  username: '',
  dateOfBirth: '',
  citizenship: ''
})

function validateUsername(): boolean {
  formErrors.username = ''
  const v = form.username
  if (!v) {
    formErrors.username = t('profile.contact.fieldRequired')
    return false
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(v)) {
    formErrors.username = t('profile.additional.usernameInvalid')
    return false
  }
  return true
}

function validateRequired(field: string) {
  const val = form[field as keyof AdditionalInfo]
  formErrors[field as keyof typeof formErrors] = ''
  if (!val || (typeof val === 'string' && !val.trim())) {
    formErrors[field as keyof typeof formErrors] = t('profile.contact.fieldRequired')
  }
}

watch(() => form.defaultResumeLanguage, (newLang) => {
  if (newLang !== null && newLang === form.additionalResumeLanguage) {
    form.additionalResumeLanguage = newLang === 1 ? 2 : 1
  }
})

watch(() => form.additionalResumeLanguage, (newLang) => {
  if (newLang !== null && newLang === form.defaultResumeLanguage) {
    form.defaultResumeLanguage = newLang === 1 ? 2 : 1
  }
})

const original = ref('')

function serialize(data: AdditionalInfo): string {
  return JSON.stringify(data)
}

function isDirty(): boolean {
  return serialize({ ...form, dateOfBirth: form.dateOfBirth }) !== original.value
}

watch([form, dateOfBirth], () => {
  emit('dirtyChange', isDirty())
}, { deep: true })

async function loadData() {
  try {
    const data = await fetchAdditionalInfo()
    if (data) {
      // Map API response back to form fields
      form.username = (data.username as string) || ''
      form.defaultResumeLanguage = data.defaultResumeLanguage as number | null
      form.additionalResumeLanguage = data.additionalResumeLanguage as number | null
      form.acceptableWorkFormats = (data.acceptableWorkFormats as string[]) || []
      form.willingnessToRelocate = (data.willingnessToRelocate as string) || ''
      form.willingnessForBusinessTravel = (data.willingnessForBusinessTravel as string) || ''
      form.skills = (data.skills as string) || ''
      form.spokenLanguages = (data.spokenLanguages as string) || ''
      form.professionalAspirations = (data.professionalAspirations as string) || ''
      form.achievements = (data.achievements as string) || ''
      form.additionalContextForAI = (data.additionalContextForAI as string) || ''
      form.citizenship = (data.citizenship as string) || ''

      if (data.dateOfBirth) {
        form.dateOfBirth = data.dateOfBirth as string
        dateOfBirth.value = new Date(data.dateOfBirth as string)
      }
    }
    original.value = serialize({ ...form })
  } catch {
    // Keep defaults
  }
}

async function handleSave() {
  let valid = true
  if (!validateUsername()) valid = false
  validateRequired('citizenship')
  if (formErrors.citizenship) valid = false
  if (!dateOfBirth.value) {
    formErrors.dateOfBirth = t('profile.contact.fieldRequired')
    valid = false
  } else {
    formErrors.dateOfBirth = ''
  }

  if (!valid) return

  saving.value = true
  try {
    const payload: Record<string, unknown> = {
      username: form.username,
      defaultResumeLanguage: form.defaultResumeLanguage,
      additionalResumeLanguage: form.additionalResumeLanguage,
      acceptableWorkFormats: form.acceptableWorkFormats,
      willingnessToRelocate: form.willingnessToRelocate,
      willingnessForBusinessTravel: form.willingnessForBusinessTravel,
      skills: form.skills,
      spokenLanguages: form.spokenLanguages,
      professionalAspirations: form.professionalAspirations,
      achievements: form.achievements,
      additionalContextForAI: form.additionalContextForAI,
      dateOfBirth: dateOfBirth.value ? dateOfBirth.value.toISOString().split('T')[0] : '',
      citizenship: form.citizenship
    }

    await updateAdditionalInfo(payload)

    // Reload to get fresh data
    await loadData()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  } finally {
    saving.value = false
  }
}

defineExpose({ isDirty, loadData })

onMounted(loadData)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.additional-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-block {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
}
.block-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0 0 14px;
}
.block-fields {
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
  gap: 16px;
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
.field-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-text-muted);
  margin: 4px 0 0;
  line-height: 1.4;
}
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
}
.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.checkbox-item label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-primary);
  cursor: pointer;
}
.form-actions {
  margin-top: 4px;
  padding-top: 16px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
