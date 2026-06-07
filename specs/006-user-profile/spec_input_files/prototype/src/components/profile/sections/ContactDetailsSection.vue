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
          <InputText v-model="form.fullName" :placeholder="$t('profile.contact.fullNamePlaceholder')" class="form-input" @blur="validateField('fullName')" />
          <small v-if="errors.fullName" class="field-error">{{ errors.fullName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.professionalTitle') }} <span class="required">*</span></label>
          <InputText v-model="form.professionalTitle" :placeholder="$t('profile.contact.professionalTitlePlaceholder')" class="form-input" @blur="validateField('professionalTitle')" />
          <small v-if="errors.professionalTitle" class="field-error">{{ errors.professionalTitle }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.email') }} <span class="required">*</span></label>
          <InputText v-model="form.email" :placeholder="$t('profile.contact.emailPlaceholder')" class="form-input" @blur="validateField('email')" />
          <small v-if="errors.email" class="field-error">{{ errors.email }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.phone') }} <span class="required">*</span></label>
          <InputText v-model="form.phone" :placeholder="$t('profile.contact.phonePlaceholder')" class="form-input" @blur="validateField('phone')" />
          <small v-if="errors.phone" class="field-error">{{ errors.phone }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.location') }} <span class="required">*</span></label>
          <InputText v-model="form.location" :placeholder="$t('profile.contact.locationPlaceholder')" class="form-input" @blur="validateField('location')" />
          <small v-if="errors.location" class="field-error">{{ errors.location }}</small>
        </div>
        <div class="form-group"></div>
      </div>
      <div class="form-divider"></div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.linkedinUrl') }}</label>
          <InputText v-model="form.linkedinUrl" :placeholder="$t('profile.contact.linkedinPlaceholder')" class="form-input" @blur="validateField('linkedinUrl')" />
          <small v-if="errors.linkedinUrl" class="field-error">{{ errors.linkedinUrl }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.portfolioUrl') }}</label>
          <InputText v-model="form.portfolioUrl" :placeholder="$t('profile.contact.portfolioPlaceholder')" class="form-input" @blur="validateField('portfolioUrl')" />
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
import { getContactDetails, saveContactDetails } from '@/services/profileMockService'
import type { ContactDetails } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'

const toast = useToast()
const { t } = useI18n()
const saving = ref(false)

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const form = reactive<ContactDetails>({
  fullName: '',
  professionalTitle: '',
  email: '',
  phone: '',
  location: '',
  linkedinUrl: '',
  portfolioUrl: '',
  telegram: '',
  whatsapp: ''
})

const original = ref<string>('')

const errors = reactive<Record<string, string>>({
  fullName: '',
  professionalTitle: '',
  email: '',
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
  const v = form[field as keyof ContactDetails] as string
  errors[field] = ''
  if (field === 'email') {
    if (!v) errors.email = t('profile.contact.emailRequired')
    else if (!isValidEmail(v)) errors.email = t('profile.contact.emailInvalid')
  } else if (field === 'linkedinUrl') {
    if (v && !isValidUrl(v)) errors.linkedinUrl = t('profile.contact.urlInvalid')
  } else if (field === 'portfolioUrl') {
    if (v && !isValidUrl(v)) errors.portfolioUrl = t('profile.contact.urlInvalid')
  } else if (['fullName', 'professionalTitle', 'phone', 'location'].includes(field)) {
    if (!v) errors[field] = t('profile.contact.fieldRequired')
  }
}

function validateAll(): boolean {
  let valid = true
  const fields = ['fullName', 'professionalTitle', 'email', 'phone', 'location', 'linkedinUrl', 'portfolioUrl'] as const
  for (const f of fields) {
    validateField(f)
    if (errors[f]) valid = false
  }
  return valid
}

function serialize(data: ContactDetails): string {
  return JSON.stringify(data)
}

function loadData() {
  const data = getContactDetails()
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
  if (!validateAll()) return
  saving.value = true
  try {
    saveContactDetails({ ...form })
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
