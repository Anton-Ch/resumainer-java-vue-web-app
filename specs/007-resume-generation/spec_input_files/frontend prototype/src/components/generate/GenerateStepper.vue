<template>
  <div class="generate-stepper" role="navigation" :aria-label="stepperLabel">
    <div
      v-for="(step, idx) in steps"
      :key="idx"
      class="stepper-step"
      :class="{
        active: currentStep === idx,
        completed: currentStep > idx,
        disabled: disabledSteps.includes(idx)
      }"
      v-tooltip.top="disabledSteps.includes(idx) ? $t('generate.disabledTooltip') : ''"
    >
      <div class="step-indicator">
        <div class="step-circle">
          <i v-if="currentStep > idx" class="pi pi-check" style="font-size: 0.75rem;"></i>
          <span v-else>{{ idx + 1 }}</span>
        </div>
        <span class="step-label">{{ step.label }}</span>
      </div>
      <div v-if="idx < steps.length - 1" class="step-connector" :class="{ active: currentStep > idx }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  currentStep: number
  disabledSteps: number[]
}>()

const steps = computed(() => [
  { label: t('generate.steps.vacancy') },
  { label: t('generate.steps.settings') },
  { label: t('generate.steps.review') },
  { label: t('generate.steps.export') }
])

const stepperLabel = computed(() => `Step ${props.currentStep + 1} of ${steps.value.length}`)
</script>

<style scoped>
.generate-stepper {
  display: flex;
  align-items: flex-start;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 0;
}
.stepper-step {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
}
.step-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  padding: 0 4px;
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
  transition: all 0.2s;
}
.stepper-step.active .step-circle {
  background: #0F9D7A;
  color: #fff;
}
.stepper-step.completed .step-circle {
  background: #0F9D7A;
  color: #fff;
}
.stepper-step.disabled .step-circle {
  opacity: 0.45;
}
.stepper-step.disabled {
  cursor: not-allowed;
}
.step-label {
  font-size: 0.82rem;
  font-weight: 500;
  color: #5D718B;
  transition: color 0.2s;
}
.stepper-step.active .step-label {
  color: #10233F;
  font-weight: 600;
}
.stepper-step.disabled .step-label {
  opacity: 0.45;
}
.step-connector {
  flex: 1;
  height: 2px;
  background: #E2E6EE;
  margin: 0 8px;
  min-width: 16px;
  align-self: center;
  margin-top: -14px;
  transition: background 0.2s;
}
.step-connector.active {
  background: #0F9D7A;
}
@media (max-width: 640px) {
  .generate-stepper {
    flex-direction: column;
    align-items: stretch;
    gap: 0;
  }
  .stepper-step {
    flex: none;
    gap: 0;
  }
  .step-indicator {
    padding: 8px 0;
  }
  .step-connector {
    height: 12px;
    width: 2px;
    margin: 0 auto;
    min-width: 0;
    align-self: auto;
    margin-top: 0;
  }
}
</style>
