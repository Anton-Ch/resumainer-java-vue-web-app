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
              <label class="form-label">{{ $t('profile.additional.defaultResumeLanguage') }} <span class="required">*</span></label>
              <Select v-model="form.defaultResumeLanguage" :options="languageOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.additionalResumeLanguage') }} <span class="required">*</span></label>
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
                <Checkbox v-model="form.acceptableWorkFormats" :inputId="'wf-' + fmt.value" :value="fmt.value" :binary="false" />
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
              <label class="form-label">{{ $t('profile.additional.dateOfBirth') }}</label>
              <DatePicker v-model="form.dateOfBirth" class="form-input" :showIcon="true" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.citizenship') }}</label>
              <InputText v-model="form.citizenship" class="form-input" />
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
import { getAdditionalInfo, saveAdditionalInfo } from '@/services/profileMockService'
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
  { value: 'en', label: t('profile.additional.languageEnglish') },
  { value: 'ru', label: t('profile.additional.languageRussian') }
])

const workFormatOptions = computed(() => [
  { value: 'office', label: t('profile.additional.office') },
  { value: 'remote', label: t('profile.additional.remote') },
  { value: 'hybrid', label: t('profile.additional.hybrid') },
  { value: 'rotational', label: t('profile.additional.rotationalSchedule') }
])

const willingnessOptions = computed(() => [
  { value: 'yes', label: t('profile.additional.yes') },
  { value: 'no', label: t('profile.additional.no') },
  { value: 'negotiable', label: t('profile.additional.negotiable') }
])

const form = reactive<AdditionalInfo>({
  username: '',
  defaultResumeLanguage: '',
  additionalResumeLanguage: '',
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

const formErrors = reactive<Record<string, string>>({
  username: ''
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

watch(() => form.defaultResumeLanguage, (newLang) => {
  if (newLang && newLang === form.additionalResumeLanguage) {
    form.additionalResumeLanguage = newLang === 'en' ? 'ru' : 'en'
  }
})

watch(() => form.additionalResumeLanguage, (newLang) => {
  if (newLang && newLang === form.defaultResumeLanguage) {
    form.defaultResumeLanguage = newLang === 'en' ? 'ru' : 'en'
  }
})

const original = ref<string>('')

function serialize(data: AdditionalInfo): string {
  return JSON.stringify(data)
}

function loadData() {
  const data = getAdditionalInfo()
  Object.assign(form, data)
  original.value = serialize({ ...data })
}

function isDirty(): boolean {
  return serialize({ ...form }) !== original.value
}

watch(form, () => {
  emit('dirtyChange', isDirty())
}, { deep: true })

async function handleSave() {
  if (!validateUsername()) return
  saving.value = true
  try {
    saveAdditionalInfo({ ...form })
    original.value = serialize({ ...form })
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
