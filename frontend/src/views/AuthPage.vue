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
          <!-- Login / Register title -->
          <h1 class="auth-title">
            {{ isLoginMode ? $t('auth.loginTitle') : $t('auth.registerTitle') }}
          </h1>
          <p class="auth-subtitle">
            {{ isLoginMode ? 'Sign in to your account' : 'Create your account' }}
          </p>

          <!-- Form with slide transition -->
          <Transition name="form-slide" mode="out-in">
            <div v-if="isLoginMode" key="login-form" class="auth-form-wrapper">
              <LoginForm @success="onAuthSuccess" />
            </div>
            <div v-else key="register-form" class="auth-form-wrapper">
              <RegisterForm @success="onAuthSuccess" />
            </div>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import LoginForm from '@/components/LoginForm.vue'
import RegisterForm from '@/components/RegisterForm.vue'

const router = useRouter()

const isLoginMode = ref(true)

function switchMode(mode: string) {
  isLoginMode.value = mode === 'login'
}

function onAuthSuccess(redirectUrl: string) {
  router.push(redirectUrl)
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

.auth-form-wrapper {
  animation: formAppear 400ms var(--vue-ease-premium) both;
}

@keyframes formAppear {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
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
  .auth-form-wrapper {
    animation: none;
  }
  .gradient-bg {
    animation: none;
  }
  .info-fade-enter-active,
  .info-fade-leave-active {
    transition: none;
  }
  .form-slide-enter-active,
  .form-slide-leave-active {
    animation: none;
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
