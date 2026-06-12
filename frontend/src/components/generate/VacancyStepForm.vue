<template>
  <form @submit.prevent="onSubmit" class="vacancy-form">
    <div class="form-group">
      <label>{{ $t('generate.vacancy.vacancyTitle') }} *</label>
      <InputText v-model="form.vacancyTitle" required />
    </div>
    <div class="form-group">
      <label>{{ $t('generate.vacancy.vacancyDescription') }} *</label>
      <Textarea v-model="form.vacancyDescription" rows="6" required />
    </div>
    <div class="form-group">
      <label>{{ $t('generate.vacancy.companyName') }}</label>
      <InputText v-model="form.companyName" />
    </div>
    <div class="form-group">
      <label>{{ $t('generate.vacancy.companyDescription') }}</label>
      <Textarea v-model="form.companyDescription" rows="3" />
    </div>
    <div class="form-group">
      <label>{{ $t('generate.vacancy.additionalComments') }}</label>
      <Textarea v-model="form.additionalComments" rows="3" />
    </div>
    <div class="form-actions">
      <Button type="submit" :label="$t('common.continue')" icon="pi pi-arrow-right" iconPos="right" />
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const emit = defineEmits<{ (e: 'continue', data: any): void }>()

const form = reactive({
  vacancyTitle: '',
  vacancyDescription: '',
  companyName: '',
  companyDescription: '',
  additionalComments: ''
})

function onSubmit() {
  if (!form.vacancyTitle || !form.vacancyDescription) return
  emit('continue', { ...form })
}
</script>
