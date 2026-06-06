<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <div class="stepper-container">
        <div class="stepper-step active">
          <span class="step-circle">1</span>
          <span class="step-label">{{ $t('generate.steps.vacancy') }}</span>
        </div>
        <div class="stepper-step disabled">
          <span class="step-circle">2</span>
          <span class="step-label">{{ $t('generate.steps.settings') }}</span>
        </div>
        <div class="stepper-step disabled">
          <span class="step-circle">3</span>
          <span class="step-label">{{ $t('generate.steps.review') }}</span>
        </div>
        <div class="stepper-step disabled">
          <span class="step-circle">4</span>
          <span class="step-label">{{ $t('generate.steps.export') }}</span>
        </div>
      </div>
      <div class="placeholder-content">
        <i class="pi pi-building" style="font-size: 3rem; color: #8091A7; margin-bottom: 1rem;"></i>
        <h1>{{ $t('placeholder.title') }}</h1>
        <p class="placeholder-text">{{ $t('placeholder.text') }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.main-content {
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 36px 32px;
}
.stepper-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 32px;
  padding: 0 4px;
}
.stepper-step {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 1 auto;
  position: relative;
}
.stepper-step:not(:last-child)::after {
  content: '';
  flex: 1;
  height: 2px;
  background: #E2E6EE;
  margin: 0 8px;
  min-width: 24px;
}
.stepper-step.active:not(:last-child)::after {
  background: #0F9D7A;
}
.step-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.82rem;
  font-weight: 600;
  flex-shrink: 0;
  background: #E2E6EE;
  color: #8091A7;
  transition: background 0.2s;
}
.stepper-step.active .step-circle {
  background: #0F9D7A;
  color: #fff;
}
.stepper-step.disabled .step-circle {
  opacity: 0.5;
}
.step-label {
  font-size: 0.82rem;
  font-weight: 500;
  color: #5D718B;
  white-space: nowrap;
}
.stepper-step.active .step-label {
  color: #10233F;
  font-weight: 600;
}
.stepper-step.disabled .step-label {
  opacity: 0.5;
}
.placeholder-content {
  text-align: center;
  padding: 48px 24px;
  background: #fff;
  border: 1px solid #E2E6EE;
  border-radius: 12px;
}
.placeholder-text {
  color: #5D718B;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}
@media (max-width: 640px) {
  .main-content {
    padding: 20px 16px;
  }
  .stepper-container {
    flex-direction: column;
    align-items: stretch;
    gap: 0;
    padding: 0;
    overflow: visible;
  }
  .stepper-step {
    flex: none;
    gap: 12px;
    padding: 10px 0;
  }
  .stepper-step:not(:last-child)::after {
    content: none;
  }
  .step-label {
    white-space: normal;
  }
}
</style>
