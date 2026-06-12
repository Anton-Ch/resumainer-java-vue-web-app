<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1 class="vue-h2" style="margin-bottom: 24px;">{{ $t('generate.export.title') }}</h1>

      <GenerateStepper
        :currentStep="3"
        :disabledSteps="[]"
      />

      <div class="step-content" v-if="flow.enPublicLink">
        <ExportResult
          :isBilingual="isBilingual"
          :showEn="hasEn"
          :showRu="hasRu"
          :enPublicLink="flow.enPublicLink"
          :ruPublicLink="flow.ruPublicLink || ''"
          :coverLetterEn="coverLetterEn"
          :coverLetterRu="coverLetterRu"
          :hasCoverLetter="hasCoverLetter"
        />

        <div class="export-footer">
          <Button
            :label="$t('generate.export.generateAnother')"
            icon="pi pi-plus"
            class="p-button-success"
            @click="handleGenerateAnother"
          />
        </div>
      </div>

      <div v-else class="vue-card" style="text-align: center; padding: 48px;">
        <i class="pi pi-exclamation-triangle" style="font-size: 2rem; color: #8091A7; margin-bottom: 12px;"></i>
        <p>No PDF has been generated yet. Please save a resume first.</p>
        <Button label="Go to review" class="p-button-outlined" @click="$router.push('/generate/review')" style="margin-top: 12px;" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGenerateResumeFlow } from '@/composables/useGenerateResumeFlow'
import AppHeader from '@/components/AppHeader.vue'
import GenerateStepper from '@/components/generate/GenerateStepper.vue'
import ExportResult from '@/components/generate/ExportResult.vue'
import Button from 'primevue/button'

const router = useRouter()
const { flow, enVariants, ruVariants, isBilingual, resetFlow } = useGenerateResumeFlow()

const hasEn = computed(() => enVariants.value.length > 0)
const hasRu = computed(() => ruVariants.value.length > 0)

const coverLetterEn = computed(() => {
  const v = enVariants.value.find(v => !!v.coverLetter)
  return v?.coverLetter || ''
})

const coverLetterRu = computed(() => {
  const v = ruVariants.value.find(v => !!v.coverLetter)
  return v?.coverLetter || ''
})

const hasCoverLetter = computed(() => !!coverLetterEn.value || !!coverLetterRu.value)

function handleGenerateAnother() {
  resetFlow()
  router.push('/generate/vacancy')
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
.export-footer {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 640px) {
  .main-content {
    padding: 20px 16px;
  }
}
</style>
