<template>
  <div class="export-result">
    <!-- Success / help alert -->
    <div class="export-help vue-alert vue-alert-success" style="margin-bottom: 20px;">
      <i class="pi pi-info-circle" style="margin-top: 2px;"></i>
      <span>{{ helpText }}</span>
    </div>

    <div class="export-cards" :class="{ 'export-cards-bilingual': isBilingual }">
      <div
        v-for="item in sortedResumes"
        :key="item.savedResumeId"
        class="vue-card export-card"
      >
        <h3 class="vue-h4" style="margin-bottom: 16px;">
          <span
            class="vue-chip"
            :class="chipClass(item)"
            style="margin-right: 8px;"
          >
            {{ item.languageCode }}
          </span>
          {{ cardTitle(item) }}
        </h3>

        <div class="vue-form-group" style="margin-bottom: 16px;">
          <label class="vue-form-label">{{ $t('generate.export.publicLink') }}</label>
          <div class="link-row">
            <InputText
              :modelValue="absolutePublicUrl(item.publicUrlLink)"
              readonly
              class="public-link-input"
            />
            <Button
              :label="$t('generate.export.copyLink')"
              icon="pi pi-copy"
              class="p-button-outlined p-button-sm"
              :aria-label="$t('generate.export.copyLink')"
              @click="copyToClipboard(absolutePublicUrl(item.publicUrlLink), 'link')"
            />
          </div>
          <span class="vue-form-hint" style="margin-top: 4px;">
            {{ $t('generate.export.safeLinkHint') }}
          </span>
        </div>

        <div
          v-if="item.coverLetter"
          class="cover-letter-block"
          style="margin-bottom: 16px;"
        >
          <label class="vue-form-label" style="margin-bottom: 8px;">
            {{ $t('generate.review.fields.coverLetter') }}
          </label>
          <Textarea
            :modelValue="item.coverLetter"
            readonly
            :rows="4"
            class="review-input"
          />
          <div style="margin-top: 8px;">
            <Button
              :label="$t('generate.export.copyCoverLetter')"
              icon="pi pi-copy"
              class="p-button-outlined p-button-sm"
              :aria-label="$t('generate.export.copyCoverLetter')"
              @click="copyToClipboard(item.coverLetter || '', 'coverLetter')"
            />
          </div>
        </div>

        <div class="export-pdf-status" v-if="!item.pdfAvailable" style="margin-bottom: 12px;">
          <i class="pi pi-info-circle" style="margin-right: 4px;"></i>
          <span>{{ item.pdfMessage || $t('generate.export.pdfNotAvailable') }}</span>
        </div>

        <div class="export-actions">
          <Button
            :label="$t('generate.export.downloadPdf')"
            icon="pi pi-download"
            class="p-button-success"
            :disabled="!item.pdfAvailable"
            @click="onDownloadPdf(item)"
          />
          <Button
            :label="$t('generate.export.openPdf')"
            icon="pi pi-external-link"
            class="p-button-outlined"
            :disabled="!item.pdfAvailable"
            @click="onOpenPdf(item)"
          />
          <Button
            :label="$t('generate.export.downloadHtml')"
            icon="pi pi-file"
            class="p-button-outlined"
            @click="onDownloadHtml(item)"
          />
        </div>

        <p class="vue-body-sm" style="margin-top: 8px; color: var(--vue-text-secondary);">
          {{ $t('generate.export.downloadHtmlHelp') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import type { ExportResultDto, SavedResumeExportDto } from '@/types/generate'
import * as generateApi from '@/services/generateResumeService'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'

const { t } = useI18n()
const toast = useToast()

const props = defineProps<{ exportData: ExportResultDto }>()

const sortedResumes = computed(() => {
  const order: Record<string, number> = { EN: 0, RU: 1 }
  return [...(props.exportData.resumes || [])].sort((a, b) => {
    const left = order[a.languageCode] ?? 99
    const right = order[b.languageCode] ?? 99
    return left - right
  })
})

/** Build absolute public URL from relative backend path. */
function absolutePublicUrl(relativePath: string): string {
  if (!relativePath) return ''
  if (relativePath.startsWith('http://') || relativePath.startsWith('https://')) {
    return relativePath
  }
  return window.location.origin + relativePath
}

const isBilingual = computed(() => sortedResumes.value.length > 1)

const helpText = computed(() => {
  return isBilingual.value
    ? t('generate.export.bilingualHelp')
    : t('generate.export.singleHelp')
})

function cardTitle(item: SavedResumeExportDto): string {
  return item.languageCode === 'RU'
    ? t('generate.export.russianResume')
    : t('generate.export.englishResume')
}

function chipClass(item: SavedResumeExportDto): string {
  return item.languageCode === 'RU' ? 'chip-ru' : 'chip-en'
}

function normalizedLanguage(code: string): string {
  return String(code || '').toLowerCase()
}

function normalizedLevel(level: string): string {
  return String(level || '').toLowerCase()
}

async function copyToClipboard(text: string, type: 'link' | 'coverLetter') {
  try {
    await navigator.clipboard.writeText(text)
    toast.add({
      severity: 'success',
      summary: type === 'link'
        ? t('generate.export.linkCopied')
        : t('generate.export.coverLetterCopied'),
      life: 3000
    })
  } catch {
    toast.add({
      severity: 'error',
      summary: t('generate.export.copyFailed') || 'Failed to copy',
      life: 3000
    })
  }
}

async function onDownloadHtml(item: SavedResumeExportDto) {
  try {
    const blob = await generateApi.downloadHtmlByUrl(item.htmlDownloadUrl)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `resume_${normalizedLanguage(item.languageCode)}_${normalizedLevel(item.adaptationLevel)}.html`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch {
    toast.add({
      severity: 'error',
      summary: t('generate.export.htmlDownloadFailed') || 'HTML download failed',
      life: 3000
    })
  }
}

async function onDownloadPdf(item: SavedResumeExportDto) {
  if (!item.pdfAvailable) return
  try {
    const blob = await generateApi.downloadPdfByUrl(item.pdfDownloadUrl)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `resume_${normalizedLanguage(item.languageCode)}_${normalizedLevel(item.adaptationLevel)}.pdf`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch {
    toast.add({
      severity: 'error',
      summary: t('generate.export.pdfDownloadFailed') || 'PDF download failed',
      life: 3000
    })
  }
}

function onOpenPdf(item: SavedResumeExportDto) {
  if (!item.pdfAvailable) return
  try {
    generateApi.openPdfByUrl(item.pdfOpenUrl)
  } catch {
    toast.add({
      severity: 'error',
      summary: t('generate.export.pdfOpenFailed') || 'Failed to open PDF',
      life: 3000
    })
  }
}
</script>

<style scoped>
.export-result { }
.export-cards {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.export-cards-bilingual {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.export-card {
  display: flex;
  flex-direction: column;
}

.link-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.public-link-input {
  flex: 1;
  font-family: var(--vue-font-mono);
  font-size: 0.82rem;
}

.review-input {
  width: 100%;
}

.export-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.chip-en {
  background: #EEF4FF;
  border-color: #2F6BFF;
  color: #2F6BFF;
}

.chip-ru {
  background: #FFF7ED;
  border-color: #D97706;
  color: #D97706;
}

@media (max-width: 639px) {
  .export-cards-bilingual {
    grid-template-columns: 1fr;
  }

  .export-actions {
    flex-direction: column;
  }

  .link-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
