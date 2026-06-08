<template>
  <div v-if="visible" class="inline-record-form" ref="formRef">
    <div class="form-header">
      <h4 class="form-title">{{ isEditing ? $t(editLabelKey) : $t(addLabelKey) }}</h4>
    </div>
    <div class="form-fields">
      <div v-for="field in fields" :key="field.key" class="form-field" :class="{ 'field-full': field.fullWidth }">
        <label :for="'field-' + field.key" class="form-label">
          {{ field.label }}
          <span v-if="field.required" class="required-mark">*</span>
        </label>
        <InputText
          v-if="field.type === 'text' || field.type === 'url'"
          :id="'field-' + field.key"
          v-model="field.value"
          :placeholder="field.placeholder"
          class="form-input"
        />
        <Textarea
          v-else-if="field.type === 'textarea'"
          :id="'field-' + field.key"
          v-model="field.value"
          :placeholder="field.placeholder"
          class="form-input"
          rows="3"
        />
        <DatePicker
          v-else-if="field.type === 'date'"
          :id="'field-' + field.key"
          v-model="field.value"
          :placeholder="field.placeholder"
          dateFormat="yy-mm-dd"
          class="form-input"
          :showIcon="true"
        />
        <Checkbox
          v-else-if="field.type === 'checkbox'"
          :id="'field-' + field.key"
          v-model="field.value"
          :binary="true"
          :inputId="'field-' + field.key"
        />
      </div>
    </div>
    <div class="form-actions">
      <Button :label="$t(saveLabelKey)" icon="pi pi-check" class="p-button-success" @click="$emit('save')" />
      <Button :label="$t(cancelLabelKey)" class="p-button-text" @click="$emit('cancel')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'

export interface FormField {
  key: string
  type: 'text' | 'textarea' | 'url' | 'date' | 'checkbox'
  label: string
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  value: any
  required?: boolean
  placeholder?: string
  fullWidth?: boolean
}

const props = defineProps<{
  visible: boolean
  fields: FormField[]
  isEditing: boolean
  addLabelKey: string
  editLabelKey: string
  saveLabelKey: string
  cancelLabelKey: string
}>()

defineEmits<{
  save: []
  cancel: []
}>()

const formRef = ref<HTMLElement | null>(null)

async function scrollToForm() {
  await nextTick()
  if (formRef.value) {
    formRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

defineExpose({ scrollToForm })
</script>

<style scoped>
.inline-record-form {
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
.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
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
.required-mark {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.form-input {
  width: 100%;
}
.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--vue-border-soft);
}
</style>
