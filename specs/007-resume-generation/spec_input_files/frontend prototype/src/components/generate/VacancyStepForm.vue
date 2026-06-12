<template>
  <div class="vacancy-form">
    <div class="vue-card vacancy-main-card">
      <h2 class="vue-h4" style="margin-bottom: 8px;">{{ $t('generate.vacancy.title') }}</h2>
      <p class="vue-body-sm" style="margin-bottom: 24px;">{{ $t('generate.vacancy.subtitle') }}</p>

      <div class="form-fields">
        <div class="vue-form-group">
          <label class="vue-form-label" for="vacancyTitle">
            {{ $t('generate.vacancy.vacancyTitle') }}<span class="vue-required">*</span>
          </label>
          <InputText
            id="vacancyTitle"
            v-model="form.vacancyTitle"
            :class="{ 'vue-input-error': errors.vacancyTitle }"
            :placeholder="$t('generate.vacancy.vacancyTitle')"
            aria-required="true"
          />
          <span v-if="errors.vacancyTitle" class="vue-form-error">{{ errors.vacancyTitle }}</span>
        </div>

        <div class="vue-form-group">
          <label class="vue-form-label" for="vacancyDescription">
            {{ $t('generate.vacancy.vacancyDescription') }}<span class="vue-required">*</span>
          </label>
          <Textarea
            id="vacancyDescription"
            v-model="form.vacancyDescription"
            :class="{ 'vue-input-error': errors.vacancyDescription }"
            :placeholder="$t('generate.vacancy.vacancyDescription')"
            rows="5"
            aria-required="true"
          />
          <span v-if="errors.vacancyDescription" class="vue-form-error">{{ errors.vacancyDescription }}</span>
        </div>
      </div>
    </div>

    <div class="vue-card" style="margin-top: 20px;">
      <div class="form-fields">
        <div class="vue-form-group">
          <label class="vue-form-label" for="companyName">
            {{ $t('generate.vacancy.companyName') }}
          </label>
          <InputText
            id="companyName"
            v-model="form.companyName"
            :placeholder="$t('generate.vacancy.companyName')"
          />
        </div>

        <div class="vue-form-group">
          <label class="vue-form-label" for="companyDescription">
            {{ $t('generate.vacancy.companyDescription') }}
          </label>
          <Textarea
            id="companyDescription"
            v-model="form.companyDescription"
            :placeholder="$t('generate.vacancy.companyDescription')"
            rows="4"
          />
        </div>

        <div class="vue-form-group">
          <label class="vue-form-label" for="additionalComments">
            {{ $t('generate.vacancy.additionalComments') }}
          </label>
          <Textarea
            id="additionalComments"
            v-model="form.additionalComments"
            :placeholder="$t('generate.vacancy.additionalCommentsPlaceholder')"
            rows="3"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GenerateResumeFormState } from '@/types/generate'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'

defineProps<{
  form: GenerateResumeFormState
  errors: Record<string, string>
}>()
</script>

<style scoped>
.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.vacancy-main-card {
  margin-bottom: 0;
}
</style>
