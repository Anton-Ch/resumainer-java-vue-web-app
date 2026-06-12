<template>
  <div class="stepper">
    <div v-for="(step, index) in steps" :key="index"
         class="step" :class="{ active: index + 1 === currentStep, completed: index + 1 < currentStep }">
      <div class="step-number">{{ index + 1 < currentStep ? '✓' : index + 1 }}</div>
      <div class="step-label">{{ step.label }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  steps: { label: string; route: string }[]
  currentStep: number
}>()
</script>

<style scoped>
.stepper { display: flex; justify-content: center; gap: 1rem; margin-bottom: 2rem; }
.step { display: flex; flex-direction: column; align-items: center; gap: 0.25rem; }
.step-number { width: 2rem; height: 2rem; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #e0e0e0; color: #666; font-weight: bold; }
.step.active .step-number { background: #3b82f6; color: white; }
.step.completed .step-number { background: #22c55e; color: white; }
.step-label { font-size: 0.8rem; color: #666; }
</style>
