<template>
  <div>
    <h1 class="page-h1">{{ t('nav.generateResume') }}</h1>
    <div class="guided-block">
      <div class="stepper-container">
        <div v-for="(step, i) in steps" :key="i" :class="['stepper-step', step.state]">
          <div class="step-circle" v-tooltip.top="step.state === 'disabled' ? t('generate.disabledTooltip') : ''">
            <template v-if="step.state==='completed'"><i class="pi pi-check" style="font-size:0.75rem" /></template>
            <template v-else>{{ i+1 }}</template>
          </div>
          <span class="step-label">{{ step.label }}</span>
        </div>
      </div>
      <div class="generate-content">
        <div v-if="generating">
          <i class="pi pi-spin pi-spinner" style="font-size:2.5rem;color:var(--accent);margin-bottom:16px;display:block" />
          <h3 style="font-family:var(--font-display);font-size:1.1rem;margin-bottom:8px">{{ t('generate.loadingTitle') }}</h3>
          <p style="color:var(--muted);font-size:0.9rem">{{ t('generate.loadingText') }}</p>
        </div>
        <div v-else>
          <p style="color:var(--muted);font-size:0.9rem;margin-bottom:16px">{{ t('placeholder.text') }}</p>
          <p-button :label="t('home.ready.generate.cta')" icon="pi pi-file-plus" class="p-button-success" @click="startGenerate" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, computed } from 'vue'

const t = inject('t')
const generating = ref(false)
const genState = ref({ vacancy: 'active', settings: 'disabled', review: 'disabled', export: 'disabled' })

const steps = computed(() => [
  { label: t('generate.steps.vacancy'), state: genState.value.vacancy },
  { label: t('generate.steps.settings'), state: genState.value.settings },
  { label: t('generate.steps.review'), state: genState.value.review },
  { label: t('generate.steps.export'), state: genState.value.export }
])

const startGenerate = () => {
  generating.value = true
  genState.value = { vacancy: 'completed', settings: 'completed', review: 'active', export: 'disabled' }
  setTimeout(() => {
    genState.value = { vacancy: 'completed', settings: 'completed', review: 'completed', export: 'active' }
    generating.value = false
  }, 3000)
}
</script>
