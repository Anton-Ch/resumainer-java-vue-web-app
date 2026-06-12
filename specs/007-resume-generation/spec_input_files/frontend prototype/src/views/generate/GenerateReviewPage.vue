<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.review.title') }}</h1>

      <GenerateStepper
        :currentStep="2"
        :disabledSteps="[3]"
      />

      <div class="step-content" v-if="hasVariants">
        <ReviewStepForm
          :enVariants="enVariants"
          :ruVariants="ruVariants"
          :isBilingual="isBilingual"
          :showLevels="isAllLevels"
          v-model:activeTab="activeTab"
          :selectedLevel="flow.selectedSaveLevel"
          @update:selectedLevel="onLevelChange"
          @save="handleSavePdf"
        />
      </div>

      <div v-else class="vue-card" style="text-align: center; padding: 48px;">
        <i class="pi pi-exclamation-triangle" style="font-size: 2rem; color: #8091A7; margin-bottom: 12px;"></i>
        <p>No generated resume found. Please generate one first.</p>
        <Button label="Go to settings" class="p-button-outlined" @click="$router.push('/generate/settings')" style="margin-top: 12px;" />
      </div>
    </main>

    <!-- PROTOTYPE MOCK ONLY — THIS LOADER SIMULATES PDF CONVERSION FOR UX REVIEW ONLY. -->
    <WhimsicalLoader
      v-if="flow.isSavingPdf"
      :title="$t('generate.loader.pdfTitle')"
      :hint="$t('generate.loader.text')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import type { AdaptationLevel } from '@/types/generate'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import ReviewStepForm from '@/components/generate/ReviewStepForm.vue'
import WhimsicalLoader from '@/components/generate/WhimsicalLoader.vue'
import Button from 'primevue/button'

const router = useRouter()
const { flow, enVariants, ruVariants, isBilingual, isAllLevels, saveToPdf } = useGenerateResumeFlow()

const activeTab = ref(0)
const hasVariants = computed(() => flow.variants.length > 0)

function onLevelChange(val: AdaptationLevel) {
  flow.selectedSaveLevel = val
}

async function handleSavePdf() {
  // PROTOTYPE MOCK ONLY — THIS SAVE TO PDF FLOW DOES NOT CALL JAVA BACKEND.
  // REPLACE WITH REAL UPDATE RESPONSE + HTML TEMPLATE RENDERING + PDF GENERATION API CALL.
  // PROTOTYPE MOCK ONLY — DO NOT KEEP THIS TIMEOUT IN PRODUCTION.
  await saveToPdf()
  router.push('/generate/export')
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.main-content {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 36px 32px;
}
.step-content {
  margin-top: 32px;
}
@media (max-width: 640px) {
  .main-content {
    padding: 20px 16px;
  }
}
</style>
