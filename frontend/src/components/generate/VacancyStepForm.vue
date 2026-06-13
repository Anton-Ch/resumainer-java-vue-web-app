<template>
  <div class="vacancy-form">
    <div class="vue-card">
      <h3 class="vue-h4" style="margin-bottom:8px;">{{ $t('generate.vacancy.vacancyBlockTitle') }}</h3>
      <p class="vue-body-sm" style="margin-bottom:20px;">{{ $t('generate.vacancy.vacancyBlockDescription') }}</p>

      <div class="form-fields">
        <div class="vue-form-group">
          <label class="vue-form-label">{{ $t('generate.vacancy.vacancyTitle') }} <span class="vue-required">*</span></label>
          <InputText v-model="form.vacancyTitle" :placeholder="$t('generate.vacancy.vacancyTitlePlaceholder')" class="field-input" />
        </div>
        <div class="vue-form-group">
          <label class="vue-form-label">{{ $t('generate.vacancy.vacancyDescription') }} <span class="vue-required">*</span></label>
          <Textarea v-model="form.vacancyDescription" :placeholder="$t('generate.vacancy.vacancyDescriptionPlaceholder')" rows="7" class="field-input" />
        </div>
      </div>
    </div>

    <div class="vue-card">
      <h3 class="vue-h4" style="margin-bottom:8px;">{{ $t('generate.vacancy.companyBlockTitle') }}</h3>
      <p class="vue-body-sm" style="margin-bottom:20px;">{{ $t('generate.vacancy.companyBlockDescription') }}</p>

      <div class="form-fields">
        <div class="vue-form-group">
          <label class="vue-form-label">{{ $t('generate.vacancy.companyName') }}</label>
          <InputText v-model="form.companyName" :placeholder="$t('generate.vacancy.companyNamePlaceholder')" class="field-input" />
        </div>
        <div class="vue-form-group">
          <label class="vue-form-label">{{ $t('generate.vacancy.companyDescription') }}</label>
          <Textarea v-model="form.companyDescription" :placeholder="$t('generate.vacancy.companyDescriptionPlaceholder')" rows="4" class="field-input" />
        </div>
        <div class="vue-form-group">
          <label class="vue-form-label">{{ $t('generate.vacancy.additionalComments') }}</label>
          <Textarea v-model="form.additionalComments" :placeholder="$t('generate.vacancy.additionalCommentsPlaceholder')" rows="3" class="field-input" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'

const form = reactive({ vacancyTitle: '', vacancyDescription: '', companyName: '', companyDescription: '', additionalComments: '' })
defineExpose({ form })
</script>

<style scoped>
.vacancy-form { display: flex; flex-direction: column; gap: 24px; }
.form-fields { display: flex; flex-direction: column; gap: 16px; }
.field-input { width: 100%; }
</style>
