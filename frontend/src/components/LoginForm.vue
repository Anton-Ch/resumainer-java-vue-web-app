<template>
  <Form :resolver :initialValues @submit="onSubmit" class="auth-form">
    <div class="form-fields">
      <!-- Email field -->
      <FormField v-slot="$field" name="email">
        <div class="form-field-inner">
          <label class="form-label" for="login-email">{{ $t('auth.email') }}</label>
          <InputText
            id="login-email"
            name="email"
            type="email"
            :placeholder="$t('auth.email')"
            :class="{ 'p-invalid': $field?.invalid }"
            fluid
          />
          <Message
            v-if="$field?.invalid"
            severity="error"
            size="small"
            variant="simple"
          >{{ $field.error?.message }}</Message>
        </div>
      </FormField>

      <!-- Password field -->
      <FormField v-slot="$field" name="password">
        <div class="form-field-inner">
          <label class="form-label" for="login-password">{{ $t('auth.password') }}</label>
          <Password
            id="login-password"
            name="password"
            :placeholder="$t('auth.password')"
            :feedback="false"
            toggleMask
            :class="{ 'p-invalid': $field?.invalid }"
            fluid
          />
          <Message
            v-if="$field?.invalid"
            severity="error"
            size="small"
            variant="simple"
          >{{ $field.error?.message }}</Message>
        </div>
      </FormField>

      <!-- Remember me -->
      <FormField v-slot="$field" name="rememberMe">
        <div class="remember-row">
          <Checkbox
            name="rememberMe"
            :binary="true"
            :inputId="'login-remember'"
          />
          <label :for="'login-remember'" class="remember-label">{{ $t('auth.rememberMe') }}</label>
        </div>
      </FormField>

      <!-- Submit button -->
      <button
        type="submit"
        class="vue-btn vue-btn-primary vue-btn-lg auth-submit-btn"
        :disabled="submitting"
      >
        <span v-if="submitting" class="spinner"></span>
        <span v-else>{{ $t('auth.login') }}</span>
      </button>
    </div>
  </Form>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Form, FormField } from '@primevue/forms'
import { zodResolver } from '@primevue/forms/resolvers/zod'
import { z } from 'zod'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Checkbox from 'primevue/checkbox'
import Message from 'primevue/message'
import { useAuth } from '@/composables/useAuth'

const emit = defineEmits<{
  success: [redirectUrl: string]
}>()

const { t, locale } = useI18n()
const { login, loading } = useAuth()
const submitting = ref(false)
const resolver = ref(createResolver(t))

// Re-create resolver when locale changes
watch(locale, () => {
  resolver.value = createResolver(t)
})

function createResolver(t: (key: string) => string) {
  return zodResolver(
    z.object({
      email: z
        .string()
        .min(1, { message: t('auth.error.emailRequired') })
        .email({ message: t('auth.error.emailInvalid') }),
      password: z
        .string()
        .min(1, { message: t('auth.error.passwordRequired') }),
      rememberMe: z.boolean().optional()
    })
  )
}

const initialValues = {
  email: '',
  password: '',
  rememberMe: false
}

async function onSubmit({ valid, values }: { valid: boolean; values: Record<string, unknown> }) {
  if (!valid) return

  submitting.value = true
  try {
    // Access values from PrimeVue Form - they come as snake_case from field names
    const formValues = values as { email: string; password: string; rememberMe: boolean }
    const response = await login(formValues.email, formValues.password, formValues.rememberMe)
    if (response.success) {
      emit('success', response.redirectUrl || '/home')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-form {
  width: 100%;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.form-field-inner {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-label {
  font-family: var(--vue-font-body);
  font-size: 14px;
  font-weight: 600;
  color: var(--vue-text-secondary);
}

.remember-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.remember-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--vue-text-secondary);
  cursor: pointer;
}

.auth-submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 4px;
}

.spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 600ms linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
