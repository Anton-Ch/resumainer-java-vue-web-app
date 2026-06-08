<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.contact.title')"
      :purpose="$t('profile.contact.purpose')"
    />

    <div class="contact-form">
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.fullName') }} <span class="required">*</span></label>
          <InputText v-model="form.fullName" :placeholder="$t('profile.contact.fullNamePlaceholder')" class="form-input" @blur="validateField('fullName')" :class="{ 'p-invalid': errors.fullName }" />
          <small v-if="errors.fullName" class="field-error">{{ errors.fullName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.professionalTitle') }} <span class="required">*</span></label>
          <InputText v-model="form.professionalTitle" :placeholder="$t('profile.contact.professionalTitlePlaceholder')" class="form-input" @blur="validateField('professionalTitle')" :class="{ 'p-invalid': errors.professionalTitle }" />
          <small v-if="errors.professionalTitle" class="field-error">{{ errors.professionalTitle }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.email') }} <span class="required">*</span></label>
          <InputText v-model="form.resumeEmail" :placeholder="$t('profile.contact.emailPlaceholder')" class="form-input" @blur="validateField('resumeEmail')" :class="{ 'p-invalid': errors.resumeEmail }" />
          <small v-if="errors.resumeEmail" class="field-error">{{ errors.resumeEmail }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.phone') }} <span class="required">*</span></label>
          <InputText v-model="form.phone" :placeholder="$t('profile.contact.phonePlaceholder')" class="form-input" @blur="validateField('phone')" :class="{ 'p-invalid': errors.phone }" />
          <small v-if="errors.phone" class="field-error">{{ errors.phone }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.location') }} <span class="required">*</span></label>
          <InputText v-model="form.location" :placeholder="$t('profile.contact.locationPlaceholder')" class="form-input" @blur="validateField('location')" :class="{ 'p-invalid': errors.location }" />
          <small v-if="errors.location" class="field-error">{{ errors.location }}</small>
        </div>
        <div class="form-group"></div>
      </div>
      <div class="form-divider"></div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.linkedinUrl') }}</label>
          <InputText v-model="form.linkedinUrl" :placeholder="$t('profile.contact.linkedinPlaceholder')" class="form-input" @blur="validateField('linkedinUrl')" :class="{ 'p-invalid': errors.linkedinUrl }" />
          <small v-if="errors.linkedinUrl" class="field-error">{{ errors.linkedinUrl }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.portfolioUrl') }}</label>
          <InputText v-model="form.portfolioUrl" :placeholder="$t('profile.contact.portfolioPlaceholder')" class="form-input" @blur="validateField('portfolioUrl')" :class="{ 'p-invalid': errors.portfolioUrl }" />
          <small v-if="errors.portfolioUrl" class="field-error">{{ errors.portfolioUrl }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.telegram') }}</label>
          <InputText v-model="form.telegram" :placeholder="$t('profile.contact.telegramPlaceholder')" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.whatsapp') }}</label>
          <InputText v-model="form.whatsapp" :placeholder="$t('profile.contact.whatsappPlaceholder')" class="form-input" />
        </div>
      </div>

      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.contact.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import { fetchContactDetails, updateContactDetails } from '@/services/profileService'
import type { ContactDetails } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'

const toast = useToast()
const { t } = useI18n()
const saving = ref(false)
const loading = ref(true)
const loadError = ref(false)

const emit = defineEmits<{
  saved: []
  'dirty-change': [dirty: boolean]
}>()

const form = reactive<ContactDetails>({
  fullName: '',
  professionalTitle: '',
  resumeEmail: '',
  phone: '',
  location: '',
  linkedinUrl: '',
  portfolioUrl: '',
  telegram: '',
  whatsapp: ''
})

const original = ref('')

const errors = reactive<Record<string, string>>({
  fullName: '',
  professionalTitle: '',
  resumeEmail: '',
  phone: '',
  location: '',
  linkedinUrl: '',
  portfolioUrl: ''
})

function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function isValidUrl(url: string): boolean {
  if (!url) return true
  return /^(https?:\/\/)?[\w\-]+(\.[\w\-]+)+[/#?]?.*$/.test(url)
}

function validateField(field: keyof typeof errors) {
  const val = form[field as keyof ContactDetails] as string
  errors[field] = ''
  if (field === 'resumeEmail') {
    if (!val) errors.resumeEmail = t('profile.contact.emailRequired')
    else if (!isValidEmail(val)) errors.resumeEmail = t('profile.contact.emailInvalid')
  } else if (field === 'linkedinUrl' || field === 'portfolioUrl') {
    if (val && !isValidUrl(val)) errors[field] = t('profile.contact.urlInvalid')
  } else if (['fullName', 'professionalTitle', 'phone', 'location'].includes(field)) {
    if (!val) errors[field] = t('profile.contact.fieldRequired')
  }
}

function validateAll(): boolean {
  let valid = true
  const fields = ['fullName', 'professionalTitle', 'resumeEmail', 'phone', 'location', 'linkedinUrl', 'portfolioUrl'] as const
  for (const f of fields) {
    validateField(f)
    if (errors[f]) valid = false
  }
  return valid
}

async function loadData() {
  loading.value = true
  loadError.value = false
  try {
    const data = await fetchContactDetails()
    if (data) {
      form.fullName = data.fullName || ''
      form.professionalTitle = data.professionalTitle || ''
      form.resumeEmail = data.resumeEmail || ''
      form.phone = data.phone || ''
      form.location = data.location || ''
      form.linkedinUrl = data.linkedinUrl || ''
      form.portfolioUrl = data.portfolioUrl || ''
      form.telegram = data.telegram || ''
      form.whatsapp = data.whatsapp || ''
    }
    original.value = JSON.stringify({ ...form })
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function isDirty(): boolean {
  return JSON.stringify({ ...form }) !== original.value
}

watch(form, () => {
  emit('dirty-change', isDirty())
}, { deep: true })

async function handleSave() {
  if (!validateAll()) return
  saving.value = true
  try {
    await updateContactDetails({ ...form })
    original.value = JSON.stringify({ ...form })
    emit('saved')
    emit('dirty-change', false)
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
.contact-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
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
.form-divider {
  border-top: 1px solid var(--vue-border-soft);
  margin: 4px 0;
}
.form-actions {
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
