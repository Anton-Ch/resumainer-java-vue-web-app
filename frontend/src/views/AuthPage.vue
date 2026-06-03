<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- Left panel: gradient + product info -->
      <div class="auth-info-col">
        <div class="gradient-bg"></div>
        <div class="info-content">
          <Transition name="info-fade" mode="out-in">
            <div v-if="isLoginMode" key="login-info" class="info-texts">
              <h2 class="info-title">{{ $t('auth.loginTitle') }}</h2>
              <p class="info-desc">
                One profile. Tailored resumes for any role.<br />
                Sign in to continue your career journey.
              </p>
            </div>
            <div v-else key="register-info" class="info-texts">
              <h2 class="info-title">{{ $t('auth.registerTitle') }}</h2>
              <p class="info-desc">
                One profile. Tailored resumes for any role.<br />
                Create your account and get started.
              </p>
            </div>
          </Transition>
        </div>
      </div>

      <!-- Right panel: form card -->
      <div class="auth-form-col">
        <!-- Top row: logo + language switcher -->
        <div class="auth-top-row">
          <div class="auth-logo">
            <svg class="auth-logo-icon" viewBox="0 0 32 32" fill="none" aria-hidden="true">
              <rect x="2" y="2" width="28" height="28" rx="7" fill="#0F9D7A"/>
              <path d="M9 12h14M9 17h14M9 22h10" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/>
            </svg>
            <span class="auth-logo-text">ResumAIner</span>
          </div>
          <LanguageSwitcher />
        </div>

        <!-- Form body with transition -->
        <div class="auth-form-body">
          <!-- Error alert -->
          <Transition name="alert-slide">
            <div v-if="errorMessage" class="auth-error-alert">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5"/>
                <line x1="8" y1="5" x2="8" y2="9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="8" cy="11.5" r="0.8" fill="currentColor"/>
              </svg>
              <span>{{ errorMessage }}</span>
            </div>
          </Transition>

          <!-- Login / Register title -->
          <h1 class="auth-title">
            {{ isLoginMode ? $t('auth.loginTitle') : $t('auth.registerTitle') }}
          </h1>
          <p class="auth-subtitle">
            {{ isLoginMode ? 'Sign in to your account' : 'Create your account' }}
          </p>

          <!-- Form with slide transition -->
          <Transition name="form-slide" mode="out-in">
            <form v-if="isLoginMode" key="login-form" class="auth-form" @submit.prevent="handleLogin">
              <!-- Staggered fields -->
              <div class="form-field" :style="{ animationDelay: '0ms' }">
                <label class="form-label" for="login-email">{{ $t('auth.email') }}</label>
                <input
                  id="login-email"
                  v-model="email"
                  type="email"
                  class="form-input"
                  :class="{ 'form-input--error': fieldError === 'email' }"
                  :placeholder="$t('auth.email')"
                  autocomplete="email"
                />
              </div>
              <div class="form-field" :style="{ animationDelay: '50ms' }">
                <label class="form-label" for="login-password">{{ $t('auth.password') }}</label>
                <input
                  id="login-password"
                  v-model="password"
                  type="password"
                  class="form-input"
                  :class="{ 'form-input--error': fieldError === 'password' }"
                  :placeholder="$t('auth.password')"
                  autocomplete="current-password"
                />
              </div>
              <div class="form-field remember-row" :style="{ animationDelay: '100ms' }">
                <label class="remember-label">
                  <input type="checkbox" v-model="rememberMe" class="remember-checkbox" />
                  <span>{{ $t('auth.rememberMe') }}</span>
                </label>
              </div>
              <div class="form-field" :style="{ animationDelay: '150ms' }">
                <button
                  type="submit"
                  class="vue-btn vue-btn-primary vue-btn-lg auth-submit-btn"
                  :disabled="loading"
                >
                  <span v-if="loading" class="spinner"></span>
                  <span v-else>{{ $t('auth.login') }}</span>
                </button>
              </div>
            </form>

            <form v-else key="register-form" class="auth-form" @submit.prevent="handleRegister">
              <div class="form-field" :style="{ animationDelay: '0ms' }">
                <label class="form-label" for="reg-email">{{ $t('auth.email') }}</label>
                <input
                  id="reg-email"
                  v-model="email"
                  type="email"
                  class="form-input"
                  :class="{ 'form-input--error': fieldError === 'email' }"
                  :placeholder="$t('auth.email')"
                  autocomplete="email"
                />
              </div>
              <div class="form-field" :style="{ animationDelay: '50ms' }">
                <label class="form-label" for="reg-password">{{ $t('auth.password') }}</label>
                <input
                  id="reg-password"
                  v-model="password"
                  type="password"
                  class="form-input"
                  :class="{ 'form-input--error': fieldError === 'password' }"
                  :placeholder="$t('auth.password')"
                  autocomplete="new-password"
                />
              </div>
              <div class="form-field" :style="{ animationDelay: '100ms' }">
                <label class="form-label" for="reg-confirm">{{ $t('auth.confirmPassword') }}</label>
                <input
                  id="reg-confirm"
                  v-model="confirmPassword"
                  type="password"
                  class="form-input"
                  :class="{ 'form-input--error': fieldError === 'confirm' }"
                  :placeholder="$t('auth.confirmPassword')"
                  autocomplete="new-password"
                />
              </div>
              <div class="form-field" :style="{ animationDelay: '150ms' }">
                <button
                  type="submit"
                  class="vue-btn vue-btn-primary vue-btn-lg auth-submit-btn"
                  :disabled="loading"
                >
                  <span v-if="loading" class="spinner"></span>
                  <span v-else>{{ $t('auth.register') }}</span>
                </button>
              </div>
            </form>
          </Transition>

          <!-- Toggle link -->
          <p class="auth-toggle">
            <template v-if="isLoginMode">
              {{ $t('auth.registerLink') }}
              <button class="toggle-link" @click="switchMode('register')">{{ $t('auth.register') }}</button>
            </template>
            <template v-else>
              {{ $t('auth.loginLink') }}
              <button class="toggle-link" @click="switchMode('login')">{{ $t('auth.login') }}</button>
            </template>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const router = useRouter()
const { login, register, loading, error, successMessage, clearError } = useAuth()

const isLoginMode = ref(true)
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const rememberMe = ref(false)
const fieldError = ref<string | null>(null)
const errorMessage = ref<string | null>(null)

// Watch for auth errors
watch(error, (err) => {
  errorMessage.value = err
  if (err) {
    // Map common error messages to fields
    if (err.toLowerCase().includes('email')) {
      fieldError.value = 'email'
    } else if (err.toLowerCase().includes('password')) {
      fieldError.value = 'password'
    }
  }
})

function switchMode(mode: string) {
  isLoginMode.value = mode === 'login'
  errorMessage.value = null
  fieldError.value = null
  clearError()
}

async function handleLogin() {
  errorMessage.value = null
  fieldError.value = null
  clearError()

  if (!email.value) { fieldError.value = 'email'; return }
  if (!password.value) { fieldError.value = 'password'; return }

  try {
    const response = await login(email.value, password.value, rememberMe.value)
    if (response.success) {
      router.push(response.redirectUrl || '/home')
    }
  } catch (err: any) {
    errorMessage.value = err?.message || 'Login failed'
  }
}

async function handleRegister() {
  errorMessage.value = null
  fieldError.value = null
  clearError()

  if (!email.value) { fieldError.value = 'email'; return }
  if (!password.value) { fieldError.value = 'password'; return }
  if (password.value !== confirmPassword.value) {
    fieldError.value = 'confirm'
    errorMessage.value = 'Passwords do not match'
    return
  }

  try {
    const response = await register(email.value, password.value, confirmPassword.value)
    if (response.success) {
      router.push(response.redirectUrl || '/home')
    }
  } catch (err: any) {
    errorMessage.value = err?.message || 'Registration failed'
  }
}
</script>

<style scoped>
/* ========== Page Layout ========== */
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background: var(--vue-bg-canvas);
}

.auth-card {
  display: flex;
  width: 100%;
  max-width: 1060px;
  min-height: 600px;
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-xl);
  box-shadow: var(--vue-shadow-card);
  overflow: hidden;
}

/* ========== Info Panel (Left) ========== */
.auth-info-col {
  flex: 0 0 50%;
  background: var(--vue-accent-bg-blue);
  padding: 52px 44px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.gradient-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(15, 157, 122, 0.08) 0%,
    rgba(47, 107, 255, 0.06) 50%,
    rgba(124, 58, 237, 0.04) 100%
  );
  animation: gradientShift 8s ease-in-out infinite alternate;
}

@keyframes gradientShift {
  0% { transform: scale(1) rotate(0deg); }
  100% { transform: scale(1.15) rotate(3deg); }
}

.info-content {
  position: relative;
  z-index: 1;
  margin-top: auto;
}

.info-title {
  font-family: var(--vue-font-heading);
  font-size: 32px;
  font-weight: 700;
  color: var(--vue-text-primary);
  line-height: 1.2;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.info-desc {
  font-family: var(--vue-font-body);
  font-size: 16px;
  color: var(--vue-text-secondary);
  line-height: 1.6;
}

/* Info panel cross-fade */
.info-fade-enter-active,
.info-fade-leave-active {
  transition: opacity 240ms cubic-bezier(0.2, 0, 0, 1),
              transform 240ms cubic-bezier(0.2, 0, 0, 1);
}

.info-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.info-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ========== Form Panel (Right) ========== */
.auth-form-col {
  flex: 0 0 50%;
  display: flex;
  flex-direction: column;
  padding: 48px 44px;
  position: relative;
}

.auth-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 24px;
  flex-shrink: 0;
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.auth-logo-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.auth-logo-text {
  font-family: var(--vue-font-heading);
  font-size: 18px;
  font-weight: 700;
  color: var(--vue-text-primary);
  letter-spacing: -0.3px;
}

.auth-form-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 16px 0 0;
}

.auth-title {
  font-family: var(--vue-font-heading);
  font-size: 28px;
  font-weight: 700;
  color: var(--vue-text-primary);
  line-height: 1.2;
  margin-bottom: 6px;
}

.auth-subtitle {
  font-family: var(--vue-font-body);
  font-size: 15px;
  color: var(--vue-text-secondary);
  margin-bottom: 28px;
  line-height: 1.5;
}

/* ========== Error Alert ========== */
.auth-error-alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  background: var(--vue-accent-bg-error);
  border: 1px solid var(--vue-accent-border-error);
  border-radius: var(--vue-radius-md);
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--vue-accent-error);
  line-height: 1.4;
}

.auth-error-alert svg {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  margin-top: 1px;
}

.alert-slide-enter-active {
  animation: slideDown 200ms var(--vue-ease-standard);
}

.alert-slide-leave-active {
  animation: slideDown 200ms var(--vue-ease-standard) reverse;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== Form ========== */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.form-field {
  margin-bottom: 16px;
  animation: fieldEntrance 400ms var(--vue-ease-premium) both;
}

@keyframes fieldEntrance {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.form-label {
  display: block;
  font-family: var(--vue-font-body);
  font-size: 14px;
  font-weight: 600;
  color: var(--vue-text-secondary);
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  background: var(--vue-bg-surface);
  border: 1.5px solid var(--vue-border-control);
  border-radius: var(--vue-radius-md);
  font-family: var(--vue-font-body);
  font-size: 15px;
  font-weight: 500;
  color: var(--vue-text-primary);
  outline: none;
  transition: border-color 200ms var(--vue-ease-standard),
              box-shadow 200ms var(--vue-ease-standard);
}

.form-input::placeholder {
  color: var(--vue-text-muted);
  font-weight: 400;
}

.form-input:hover {
  border-color: #D0D8E6;
}

.form-input:focus {
  border-color: var(--vue-accent-blue);
  border-width: 1.8px;
  box-shadow: var(--vue-shadow-focus);
}

.form-input--error {
  border-color: var(--vue-accent-error);
  background: #FFF8F5;
}

.form-input--error:focus {
  border-color: var(--vue-accent-error);
  box-shadow: var(--vue-shadow-error);
}

/* Form slide transition */
.form-slide-enter-active {
  animation: formEnter 300ms var(--vue-ease-premium);
}

.form-slide-leave-active {
  animation: formLeave 200ms var(--vue-ease-standard);
}

@keyframes formEnter {
  from { opacity: 0; transform: translateX(24px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes formLeave {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(-24px); }
}

/* ========== Remember me ========== */
.remember-row {
  display: flex;
  align-items: center;
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--vue-text-secondary);
}

.remember-checkbox {
  width: 16px;
  height: 16px;
  accent-color: var(--vue-accent-primary);
  cursor: pointer;
}

/* ========== Submit Button ========== */
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

/* ========== Toggle Link ========== */
.auth-toggle {
  font-family: var(--vue-font-body);
  font-size: 14px;
  color: var(--vue-text-secondary);
  text-align: center;
  margin-top: 20px;
}

.toggle-link {
  background: none;
  border: none;
  color: var(--vue-accent-blue);
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
  text-decoration: underline;
  transition: color 150ms var(--vue-ease-standard);
}

.toggle-link:hover {
  color: var(--vue-accent-blue-hover);
}

/* ========== Reduced Motion ========== */
@media (prefers-reduced-motion: reduce) {
  .gradient-bg {
    animation: none;
  }

  .form-field {
    animation: none;
    opacity: 1;
    transform: none;
  }

  .form-slide-enter-active,
  .form-slide-leave-active {
    animation: none;
  }

  .info-fade-enter-active,
  .info-fade-leave-active {
    transition: none;
  }
}

/* ========== Mobile ========== */
@media (max-width: 768px) {
  .auth-card {
    flex-direction: column;
    min-height: auto;
    max-height: none;
  }

  .auth-info-col {
    display: none;
  }

  .auth-form-col {
    flex: 1;
    padding: 32px 24px;
  }

  .auth-page {
    padding: 16px;
    align-items: flex-start;
  }
}
</style>
