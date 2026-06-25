<template>
  <Dialog
    v-model:visible="visible"
    modal
    :header="$t('resumeDetails.title')"
    :style="{ maxWidth: '580px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
  >
    <template v-if="resume">
      <!-- Metadata grid -->
      <div class="detail-grid">
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.resumeTitle') }}</div>
          <div class="detail-value">{{ resume.resumeTitle }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.vacancy') }}</div>
          <div class="detail-value">{{ resume.vacancyTitle }}</div>
        </div>
        <div class="detail-field" v-if="resume.companyName">
          <div class="detail-label">{{ $t('home.table.company') }}</div>
          <div class="detail-value">{{ resume.companyName }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.language') }}</div>
          <div class="detail-value">{{ resume.languageCode === 'EN' ? $t('language.en') : $t('language.ru') }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.adaptationLevel') }}</div>
          <div class="detail-value">{{ $t('adaptation.' + resume.adaptationLevel?.toLowerCase()) }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.created') }}</div>
          <div class="detail-value">{{ resume.createdAt }}</div>
        </div>
      </div>

      <!-- Public link row -->
      <div v-if="resume.publicUrlLink" class="public-link-row">
        <i class="pi pi-link" style="color: #8091A7; font-size: 0.9rem;"></i>
        <span class="link-text">{{ resume.publicUrlLink }}</span>
        <Button :label="$t('resumeDetails.copyLink')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyLink" />
      </div>

      <!-- PDF unavailable message (with i18n fallback if pdfMessage is null) -->
      <div v-if="resume.pdfAvailable === false" class="pdf-unavailable-msg">
        <i class="pi pi-exclamation-triangle" style="color: #D97706;"></i>
        <span>{{ resume.pdfMessage || $t('resumeDetails.pdfNotAvailable') }}</span>
      </div>

      <!-- Cover letter -->
      <Accordion>
        <AccordionTab :header="$t('resumeDetails.coverLetter')">
          <template v-if="resume.coverLetter">
            <div class="cover-letter-content">
              <p v-if="!coverLetterExpanded && resume.coverLetter.length > 150" class="cover-letter-text">
                {{ resume.coverLetter.substring(0, 150) }}...
              </p>
              <p v-else class="cover-letter-text">{{ resume.coverLetter }}</p>
              <div class="cover-letter-actions">
                <Button
                  v-if="resume.coverLetter.length > 150"
                  :label="coverLetterExpanded ? $t('resumeDetails.hideFullCoverLetter') : $t('resumeDetails.showFullCoverLetter')"
                  icon="pi pi-chevron-down"
                  :icon-rotation="coverLetterExpanded ? 180 : 0"
                  class="p-button-text p-button-sm"
                  @click="coverLetterExpanded = !coverLetterExpanded"
                />
                <Button
                  :label="$t('resumeDetails.copyCoverLetter')"
                  icon="pi pi-copy"
                  class="p-button-text p-button-sm"
                  @click="onCopyCoverLetter"
                />
              </div>
            </div>
          </template>
          <p v-else class="empty-state-text">{{ $t('resumeDetails.noCoverLetter') }}</p>
        </AccordionTab>
      </Accordion>

      <!-- Action buttons (always visible) -->
      <div class="modal-actions">
        <div class="pdf-actions">
          <Button
            :label="$t('resumeDetails.view')"
            icon="pi pi-external-link"
            class="p-button-success"
            @click="viewResume"
            :disabled="!resume.pdfAvailable"
          />
          <Button
            :label="$t('resumeDetails.downloadPdf')"
            icon="pi pi-download"
            class="p-button-success p-button-outlined"
            @click="downloadPdf"
            :disabled="!resume.pdfAvailable"
          />
          <Button
            v-if="resume.htmlDownloadUrl"
            :label="$t('resumeDetails.downloadHtml')"
            icon="pi pi-code"
            class="p-button-success p-button-outlined"
            @click="downloadHtml"
          />
        </div>
        <Button
          :label="$t('resumeDetails.delete')"
          icon="pi pi-trash"
          class="p-button-danger p-button-outlined"
          :loading="deleteLoading"
          :disabled="deleteLoading"
          @click="confirmDelete"
        />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import type { SavedResumeData } from '@/services/userHomeService'

const props = defineProps<{
  visible: boolean
  resume: SavedResumeData | null
  deleteLoading?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  delete: [id: number]
}>()

const { t } = useI18n()
const confirm = useConfirm()
const toast = useToast()

const coverLetterExpanded = ref(false)

// Reset cover letter preview when modal opens
watch(() => props.visible, (newVal) => {
  if (newVal) {
    coverLetterExpanded.value = false
  }
})

// Computed get/set bridge for v-model:visible (FR-001)
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
})

async function onCopyLink() {
  if (!props.resume?.publicUrlLink) return
  try {
    await navigator.clipboard.writeText(props.resume.publicUrlLink)
    toast.add({ severity: 'success', summary: '', detail: t('resumeDetails.linkCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('resumeDetails.copyFailed'), life: 3000 })
  }
}

async function onCopyCoverLetter() {
  if (!props.resume?.coverLetter) return
  try {
    await navigator.clipboard.writeText(props.resume.coverLetter)
    toast.add({ severity: 'success', summary: '', detail: t('resumeDetails.coverLetterCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('resumeDetails.copyFailed'), life: 3000 })
  }
}

function viewResume() {
  if (props.resume?.pdfOpenUrl) {
    window.open(props.resume.pdfOpenUrl, '_blank')
  }
}

function downloadPdf() {
  if (props.resume?.pdfDownloadUrl) {
    const a = document.createElement('a')
    a.href = props.resume.pdfDownloadUrl
    a.download = `${props.resume.resumeTitle || 'resume'}.pdf`
    a.click()
  }
}

function downloadHtml() {
  if (props.resume?.htmlDownloadUrl) {
    const a = document.createElement('a')
    a.href = props.resume.htmlDownloadUrl
    a.download = `${props.resume.resumeTitle || 'resume'}.html`
    a.click()
  }
}

function confirmDelete() {
  if (props.deleteLoading) return
  confirm.require({
    header: t('deleteResume.title'),
    message: t('deleteResume.text'),
    rejectLabel: t('deleteResume.cancel'),
    acceptLabel: t('deleteResume.confirm'),
    accept: () => {
      if (props.resume) {
        emit('delete', props.resume.id)
      }
    },
    reject: () => {
      // Modal remains usable, no action
    }
  })
}
</script>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}
.detail-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.15rem;
}
.detail-value {
  font-size: 0.9rem;
  color: #10233F;
}
.public-link-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #F9FAFB;
  border-radius: 8px;
  margin-bottom: 1rem;
}
.link-text {
  flex: 1;
  min-width: 0;
  font-size: 0.85rem;
  color: #5D718B;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}
.pdf-unavailable-msg {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.75rem;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #92400E;
  margin-bottom: 1rem;
}
.cover-letter-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.cover-letter-text {
  font-size: 0.85rem;
  color: #374151;
  line-height: 1.5;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}
.cover-letter-actions {
  display: flex;
  gap: 0.25rem;
}
.empty-state-text {
  color: #5D718B;
  font-size: 0.9rem;
}
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 1.25rem;
  padding-top: 1rem;
  border-top: 1px solid #E5E7EB;
}
.pdf-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
@media (max-width: 480px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .modal-actions {
    flex-direction: column;
    gap: 0.75rem;
  }
  .pdf-actions {
    width: 100%;
  }
  .pdf-actions .p-button {
    flex: 1;
  }
}
</style>
