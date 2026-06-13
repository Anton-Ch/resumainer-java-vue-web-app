<template>
  <div class="export-result">
    <div class="export-cards" :class="{ 'export-cards-bilingual': exportData.resumes.length > 1 }">
      <div v-for="item in exportData.resumes" :key="item.savedResumeId" class="vue-card export-card">
        <h3 class="vue-h4" style="margin-bottom:16px;">
          <span :class="'vue-chip chip-' + item.languageCode.toLowerCase()" style="margin-right:8px;">{{ item.languageCode }}</span>
        </h3>

        <div class="vue-form-group" style="margin-bottom:16px;">
          <label class="vue-form-label">{{ $t('generate.export.publicLink') }}</label>
          <div class="link-row">
            <InputText :modelValue="item.publicUrlLink" readonly class="public-link-input" />
            <Button :label="$t('generate.export.copyLink')" icon="pi pi-copy" class="p-button-outlined p-button-sm" @click="copyToClipboard(item.publicUrlLink)" />
          </div>
        </div>

        <div v-if="item.coverLetter" class="cover-letter-block" style="margin-bottom:16px;">
          <label class="vue-form-label" style="margin-bottom:8px;">{{ $t('generate.review.fields.coverLetter') }}</label>
          <Textarea :modelValue="item.coverLetter" readonly :rows="4" class="review-input" />
          <div style="margin-top:8px;">
            <Button :label="$t('generate.export.copyCoverLetter')" icon="pi pi-copy" class="p-button-outlined p-button-sm" @click="copyToClipboard(item.coverLetter || '')" />
          </div>
        </div>

        <div class="export-actions">
          <Button :label="$t('generate.export.downloadPdf')" icon="pi pi-download" class="p-button-success" @click="onDownloadPdf(item)" />
          <Button :label="$t('generate.export.openPdf')" icon="pi pi-external-link" class="p-button-outlined" @click="onOpenPdf(item)" />
          <Button :label="$t('generate.export.downloadHtml')" icon="pi pi-file" class="p-button-outlined" @click="onDownloadHtml(item)" />
        </div>
        <p class="vue-body-sm" style="margin-top:8px;color:var(--vue-text-secondary);">{{ $t('generate.export.downloadHtmlHelp') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ExportResultDto, SavedResumeExportDto } from '@/services/generateResumeService'
import * as generateApi from '@/services/generateResumeService'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'

defineProps<{ exportData: ExportResultDto }>()

async function onDownloadHtml(item: SavedResumeExportDto) {
  try {
    const blob = await generateApi.downloadHtml(item.savedResumeId)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `resume_${item.languageCode.toLowerCase()}_${item.adaptationLevel.toLowerCase()}.html`
    a.click()
    URL.revokeObjectURL(url)
  } catch { console.error('HTML download failed') }
}

function onDownloadPdf(_item: SavedResumeExportDto) {
  // Placeholder in feat/007
}

function onOpenPdf(_item: SavedResumeExportDto) {
  // Placeholder in feat/007
}

async function copyToClipboard(text: string) {
  try { await navigator.clipboard.writeText(text) } catch { console.warn('Clipboard not available') }
}
</script>

<style scoped>
.export-result { }
.export-cards { display: flex; flex-direction: column; gap: 24px; }
.link-row { display: flex; gap: 8px; align-items: center; }
.public-link-input { flex: 1; }
.export-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.review-input { width: 100%; }
</style>
