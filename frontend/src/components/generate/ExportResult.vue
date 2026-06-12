<template>
  <div class="export-results">
    <div v-for="item in exportData.resumes" :key="item.savedResumeId" class="export-card">
      <div class="export-header">
        <span class="export-lang-badge">{{ item.languageCode }}</span>
        <span class="export-level">{{ item.adaptationLevel }}</span>
      </div>

      <div class="export-actions">
        <Button :label="$t('generate.export.downloadHtml')" icon="pi pi-download" class="p-button-outlined" @click="onDownloadHtml(item)" />

        <Button :label="$t('generate.export.copyPublicLink')" icon="pi pi-link" class="p-button-outlined" @click="onCopyLink(item)" :disabled="!item.publicUrlLink" />

        <Button :label="$t('generate.export.downloadPdf')" icon="pi pi-file-pdf" class="p-button-outlined" disabled :title="item.pdfMessage" />

        <Button :label="$t('generate.export.openPdf')" icon="pi pi-external-link" class="p-button-outlined" disabled :title="item.pdfMessage" />
      </div>

      <div v-if="item.coverLetter" class="cover-letter-section">
        <Button :label="$t('generate.export.copyCoverLetter')" icon="pi pi-copy" class="p-button-text" @click="onCopyCoverLetter(item)" />
      </div>

      <div class="pdf-notice" v-if="!item.pdfAvailable">
        <small>{{ item.pdfMessage }}</small>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ExportResultDto, SavedResumeExportDto } from '@/services/generateResumeService'
import * as generateApi from '@/services/generateResumeService'

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
  } catch {
    console.error('HTML download failed')
  }
}

async function onCopyLink(item: SavedResumeExportDto) {
  try {
    await navigator.clipboard.writeText(item.publicUrlLink)
  } catch {
    console.warn('Clipboard not available')
  }
}

async function onCopyCoverLetter(item: SavedResumeExportDto) {
  if (item.coverLetter) {
    try {
      await navigator.clipboard.writeText(item.coverLetter)
    } catch {
      console.warn('Clipboard not available')
    }
  }
}
</script>

<style scoped>
.export-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 1.5rem; margin-bottom: 1rem; }
.export-header { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 1rem; }
.export-lang-badge { background: #3b82f6; color: white; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.8rem; font-weight: bold; }
.export-level { color: #666; font-size: 0.9rem; }
.export-actions { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.pdf-notice { margin-top: 0.5rem; color: #999; }
</style>
