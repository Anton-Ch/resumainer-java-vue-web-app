<template>
  <div class="export-result">
    <div class="export-help vue-alert vue-alert-success" style="margin-bottom: 20px;">
      <i class="pi pi-info-circle" style="margin-top: 2px;"></i>
      <span>{{ helpText }}</span>
    </div>

    <div class="export-cards" :class="{ 'export-cards-bilingual': isBilingual }">
      <!-- English card -->
      <div v-if="showEn" class="vue-card export-card">
        <h3 class="vue-h4" style="margin-bottom: 16px;">
          <span class="vue-chip chip-en" style="margin-right: 8px;">EN</span>
          {{ $t('generate.export.englishResume') }}
        </h3>

        <div class="vue-form-group" style="margin-bottom: 16px;">
          <label class="vue-form-label">{{ $t('generate.export.publicLink') }}</label>
          <div class="link-row">
            <InputText :modelValue="enPublicLink" readonly class="public-link-input" />
            <Button
              :label="$t('generate.export.copyLink')"
              icon="pi pi-copy"
              class="p-button-outlined p-button-sm"
              @click="copyToClipboard(enPublicLink, 'link')"
              :aria-label="$t('generate.export.copyLink')"
            />
          </div>
          <span class="vue-form-hint" style="margin-top: 4px;">{{ $t('generate.export.safeLinkHint') }}</span>
        </div>

        <div v-if="hasCoverLetter" class="cover-letter-block" style="margin-bottom: 16px;">
          <label class="vue-form-label" style="margin-bottom: 8px;">{{ $t('generate.review.fields.coverLetter') }}</label>
          <Textarea :modelValue="coverLetterEn" readonly :rows="4" class="review-input" />
          <div style="margin-top: 8px;">
            <Button
              :label="$t('generate.export.copyCoverLetter')"
              icon="pi pi-copy"
              class="p-button-outlined p-button-sm"
              @click="copyToClipboard(coverLetterEn, 'coverLetter')"
              :aria-label="$t('generate.export.copyCoverLetter')"
            />
          </div>
        </div>

        <div class="export-actions">
          <Button
            :label="$t('generate.export.downloadPdf')"
            icon="pi pi-download"
            class="p-button-success"
            @click="downloadPdf('en')"
          />
          <Button
            :label="$t('generate.export.openPdf')"
            icon="pi pi-external-link"
            class="p-button-outlined"
            @click="openPdf(enPublicLink)"
          />
          <Button
            :label="$t('generate.export.downloadHtml')"
            icon="pi pi-file"
            class="p-button-outlined"
            @click="downloadHtml('en')"
          />
        </div>
        <p class="vue-body-sm" style="margin-top: 8px; color: var(--vue-text-secondary);">
          {{ $t('generate.export.downloadHtmlHelp') }}
        </p>
      </div>

      <!-- Russian card -->
      <div v-if="showRu" class="vue-card export-card">
        <h3 class="vue-h4" style="margin-bottom: 16px;">
          <span class="vue-chip chip-ru" style="margin-right: 8px;">RU</span>
          {{ $t('generate.export.russianResume') }}
        </h3>

        <div class="vue-form-group" style="margin-bottom: 16px;">
          <label class="vue-form-label">{{ $t('generate.export.publicLink') }}</label>
          <div class="link-row">
            <InputText :modelValue="ruPublicLink" readonly class="public-link-input" />
            <Button
              :label="$t('generate.export.copyLink')"
              icon="pi pi-copy"
              class="p-button-outlined p-button-sm"
              @click="copyToClipboard(ruPublicLink, 'link')"
              :aria-label="$t('generate.export.copyLink')"
            />
          </div>
          <span class="vue-form-hint" style="margin-top: 4px;">{{ $t('generate.export.safeLinkHint') }}</span>
        </div>

        <div v-if="hasCoverLetter" class="cover-letter-block" style="margin-bottom: 16px;">
          <label class="vue-form-label" style="margin-bottom: 8px;">{{ $t('generate.review.fields.coverLetter') }}</label>
          <Textarea :modelValue="coverLetterRu" readonly :rows="4" class="review-input" />
          <div style="margin-top: 8px;">
            <Button
              :label="$t('generate.export.copyCoverLetter')"
              icon="pi pi-copy"
              class="p-button-outlined p-button-sm"
              @click="copyToClipboard(coverLetterRu, 'coverLetter')"
              :aria-label="$t('generate.export.copyCoverLetter')"
            />
          </div>
        </div>

        <div class="export-actions">
          <Button
            :label="$t('generate.export.downloadPdf')"
            icon="pi pi-download"
            class="p-button-success"
            @click="downloadPdf('ru')"
          />
          <Button
            :label="$t('generate.export.openPdf')"
            icon="pi pi-external-link"
            class="p-button-outlined"
            @click="openPdf(ruPublicLink)"
          />
          <Button
            :label="$t('generate.export.downloadHtml')"
            icon="pi pi-file"
            class="p-button-outlined"
            @click="downloadHtml('ru')"
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
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'

const { t } = useI18n()
const toast = useToast()

const props = defineProps<{
  isBilingual: boolean
  showEn: boolean
  showRu: boolean
  enPublicLink: string
  ruPublicLink: string
  coverLetterEn: string
  coverLetterRu: string
  hasCoverLetter: boolean
}>()

const helpText = computed(() => {
  if (props.isBilingual) return t('generate.export.bilingualHelp')
  return t('generate.export.singleHelp')
})

function copyToClipboard(text: string, type: 'link' | 'coverLetter') {
  navigator.clipboard.writeText(text).then(() => {
    toast.add({
      severity: 'success',
      summary: type === 'link' ? t('generate.export.linkCopied') : t('generate.export.coverLetterCopied'),
      life: 3000
    })
  }).catch(() => {
    toast.add({
      severity: 'error',
      summary: 'Failed to copy',
      life: 3000
    })
  })
}

function downloadPdf(lang: string) {
  // PROTOTYPE MOCK ONLY — THIS DOES NOT ACTUALLY DOWNLOAD A PDF.
  // REPLACE WITH REAL PDF BLOB DOWNLOAD IN PRODUCTION.
  const link = lang === 'en' ? props.enPublicLink : props.ruPublicLink
  toast.add({
    severity: 'info',
    summary: `PDF download initiated: ${link}`,
    life: 3000
  })
}

function openPdf(url: string) {
  // PROTOTYPE MOCK ONLY — THIS OPENS A MOCK URL. REPLACE WITH REAL PDF URL.
  window.open(url, '_blank', 'noopener,noreferrer')
}

function downloadHtml(lang: string) {
  // PROTOTYPE MOCK ONLY — HTML DOWNLOAD WILL USE saved_resume.html_file_path FROM BACKEND.
  // REPLACE WITH REAL HTML BLOB DOWNLOAD IN PRODUCTION.
  toast.add({
    severity: 'info',
    summary: `HTML download initiated (${lang}): will use backend saved_resume.html_file_path`,
    life: 3000
  })
}
</script>

<style scoped>
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
@media (max-width: 639px) {
  .export-cards-bilingual {
    grid-template-columns: 1fr;
  }
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
  .export-actions {
    flex-direction: column;
  }
  .link-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
