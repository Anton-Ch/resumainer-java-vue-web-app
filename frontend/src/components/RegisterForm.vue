<template>
  <Form :resolver :initialValues @submit="onSubmit" class="auth-form">
    <div class="form-fields">
      <!-- Email field -->
      <FormField v-slot="$field" name="email">
        <div class="form-field-inner">
          <label class="form-label" for="reg-email">{{ $t('auth.email') }}</label>
          <InputText
            id="reg-email"
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
          <label class="form-label" for="reg-password">{{ $t('auth.password') }}</label>
          <Password
            id="reg-password"
            name="password"
            :placeholder="$t('auth.password')"
            :feedback="true"
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

      <!-- Confirm password field -->
      <FormField v-slot="$field" name="confirmPassword">
        <div class="form-field-inner">
          <label class="form-label" for="reg-confirm">{{ $t('auth.confirmPassword') }}</label>
          <Password
            id="reg-confirm"
            name="confirmPassword"
            :placeholder="$t('auth.confirmPassword')"
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

      <!-- General error message -->
      <Message
        v-if="generalError"
        severity="error"
        size="small"
        variant="simple"
        class="general-error"
      >{{ generalError }}</Message>

      <!-- Submit button -->
      <button
        type="submit"
        class="vue-btn vue-btn-primary vue-btn-lg auth-submit-btn"
        :disabled="submitting"
      >
        <span v-if="submitting" class="spinner"></span>
        <span v-else>{{ $t('auth.register') }}</span>
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
import Message from 'primevue/message'
import { useAuth } from '@/composables/useAuth'

const emit = defineEmits<{
  success: [redirectUrl: string]
}>()

const { t, locale } = useI18n()
const { register, loading } = useAuth()
const submitting = ref(false)
const generalError = ref('')
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
        .min(8, { message: t('auth.error.passwordMinLength') })
        .refine((val) => /[A-Z]/.test(val), {
          message: t('auth.error.passwordStrength')
        })
        .refine((val) => /[a-z]/.test(val), {
          message: t('auth.error.passwordStrength')
        })
        .refine((val) => /\d/.test(val), {
          message: t('auth.error.passwordStrength')
        }),
      confirmPassword: z
        .string()
        .min(1, { message: t('auth.error.confirmPasswordRequired') })
    }).refine((data) => data.password === data.confirmPassword, {
      message: t('auth.error.passwordMismatch'),
      path: ['confirmPassword']
    })
  )
}

const initialValues = {
  email: '',
  password: '',
  confirmPassword: ''
}

async function onSubmit({ valid, values }: { valid: boolean; values: Record<string, unknown> }) {
  if (!valid) return

  submitting.value = true
  generalError.value = ''
  try {
    const formValues = values as { email: string; password: string; confirmPassword: string }
    const response = await register(formValues.email, formValues.password, formValues.confirmPassword)
    if (response.success) {
      emit('success', response.redirectUrl || '/home')
    }
  } catch (err: unknown) {
    const data = err as { message?: string }
    generalError.value = data.message || t('auth.error.serverError')
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

.auth-submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 4px;
}

.general-error {
  margin-bottom: 12px;
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
