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
import { ref } from 'vue'
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

const { register, loading } = useAuth()
const submitting = ref(false)

const resolver = zodResolver(
  z.object({
    email: z
      .string()
      .min(1, { message: 'Email is required' })
      .email({ message: 'Invalid email format' }),
    password: z
      .string()
      .min(8, { message: 'Password must be at least 8 characters' })
      .refine((val) => /[A-Z]/.test(val), {
        message: 'Must contain an uppercase letter'
      })
      .refine((val) => /[a-z]/.test(val), {
        message: 'Must contain a lowercase letter'
      })
      .refine((val) => /\d/.test(val), {
        message: 'Must contain a digit'
      }),
    confirmPassword: z
      .string()
      .min(1, { message: 'Please confirm your password' })
  }).refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword']
  })
)

const initialValues = {
  email: '',
  password: '',
  confirmPassword: ''
}

async function onSubmit({ valid, values }: { valid: boolean; values: Record<string, unknown> }) {
  if (!valid) return

  submitting.value = true
  try {
    const formValues = values as { email: string; password: string; confirmPassword: string }
    const response = await register(formValues.email, formValues.password, formValues.confirmPassword)
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
