<template>
  <Dialog
    v-model:visible="visible"
    modal
    :header="$t('admin.resumeDetails.title')"
    :style="{ maxWidth: '620px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
  >
    <template v-if="resume">
      <!-- Owner info -->
      <div class="detail-section">
        <h4 class="section-title">{{ $t('admin.resumeDetails.ownerInfo') }}</h4>
        <div class="detail-grid">
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumeDetails.username') }}</span>
            <span class="detail-value">{{ resume.ownerUsername }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumeDetails.email') }}</span>
            <span class="detail-value">{{ resume.ownerEmail }}</span>
          </div>
          <div class="detail-field" v-if="resume.ownerFullName">
            <span class="detail-label">{{ $t('admin.resumeDetails.fullName') }}</span>
            <span class="detail-value">{{ resume.ownerFullName }}</span>
          </div>
        </div>
      </div>

      <!-- Resume metadata -->
      <div class="detail-section">
        <h4 class="section-title">{{ $t('admin.resumeDetails.resumeInfo') }}</h4>
        <div class="detail-grid">
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumes.resumeTitle') }}</span>
            <span class="detail-value">{{ resume.resumeTitle }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumes.vacancy') }}</span>
            <span class="detail-value">{{ resume.vacancyTitle }}</span>
          </div>
          <div class="detail-field" v-if="resume.companyName">
            <span class="detail-label">{{ $t('admin.resumes.company') }}</span>
            <span class="detail-value">{{ resume.companyName }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumes.language') }}</span>
            <span class="detail-value">{{ resume.languageCode === 'EN' ? $t('language.en') : $t('language.ru') }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumes.adaptationLevel') }}</span>
            <span class="detail-value">{{ $t('adaptation.' + resume.adaptationLevel?.toLowerCase()) }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">{{ $t('admin.resumes.created') }}</span>
            <span class="detail-value">{{ resume.createdAt }}</span>
          </div>
        </div>
      </div>

      <!-- PDF status -->
      <div v-if="!resume.pdfAvailable && resume.pdfMessage" class="pdf-unavailable-msg">
        <i class="pi pi-exclamation-triangle" style="color: #D97706;"></i>
        <span>{{ resume.pdfMessage }}</span>
      </div>

      <!-- Public link -->
      <div v-if="resume.publicUrlLink" class="public-link-row">
        <i class="pi pi-link" style="color: #8091A7;"></i>
        <span class="link-text">{{ resume.publicUrlLink }}</span>
        <Button :label="$t('admin.resumeDetails.copyLink')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyLink" />
      </div>

      <!-- Cover letter -->
      <Accordion>
        <AccordionTab :header="$t('admin.resumeDetails.coverLetter')">
          <p v-if="resume.coverLetter" class="cover-letter-text">{{ resume.coverLetter }}</p>
          <p v-else class="empty-text">{{ $t('admin.resumeDetails.noCoverLetter') }}</p>
        </AccordionTab>
      </Accordion>

      <!-- Actions: PDF + HTML + Delete -->
      <div class="modal-actions">
        <div class="pdf-actions">
          <Button
            :label="$t('admin.resumeDetails.openPdf')"
            icon="pi pi-external-link"
            class="p-button-success"
            @click="openPdf"
            :disabled="!resume.pdfAvailable"
          />
          <Button
            :label="$t('admin.resumeDetails.downloadPdf')"
            icon="pi pi-download"
            class="p-button-success p-button-outlined"
            @click="downloadPdf"
            :disabled="!resume.pdfAvailable"
          />
          <Button
            v-if="resume.htmlDownloadUrl"
            :label="$t('admin.resumeDetails.downloadHtml')"
            icon="pi pi-code"
            class="p-button-success p-button-outlined"
            @click="downloadHtml"
          />
        </div>
        <Button
          :label="$t('admin.resumeDetails.delete')"
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
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import type { AdminSavedResume } from '@/types/admin'

const props = defineProps<{
  visible: boolean
  resume: AdminSavedResume | null
  deleteLoading?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  delete: [id: number]
}>()

const { t } = useI18n()
const confirm = useConfirm()
const toast = useToast()

const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
})

async function onCopyLink() {
  if (!props.resume?.publicUrlLink) return
  try {
    await navigator.clipboard.writeText(props.resume.publicUrlLink)
    toast.add({ severity: 'success', summary: '', detail: t('admin.resumeDetails.linkCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('admin.resumeDetails.copyFailed'), life: 3000 })
  }
}

function openPdf() {
  if (props.resume?.pdfOpenUrl) window.open(props.resume.pdfOpenUrl, '_blank')
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
  if (props.deleteLoading || !props.resume) return
  confirm.require({
    header: t('admin.resumeDetails.deleteTitle'),
    message: t('admin.resumeDetails.deleteText'),
    rejectLabel: t('admin.resumeDetails.cancel'),
    acceptLabel: t('admin.resumeDetails.confirm'),
    accept: () => {
      if (props.resume) emit('delete', props.resume.id)
    }
  })
}
</script>

<style scoped>
.detail-section {
  margin-bottom: 1rem;
}
.section-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 0.5rem;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}
.detail-field {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}
.detail-label {
  font-size: 0.7rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.detail-value {
  font-size: 0.9rem;
  color: #10233F;
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
  overflow-wrap: anywhere;
  word-break: break-word;
}
.cover-letter-text {
  font-size: 0.85rem;
  color: #374151;
  line-height: 1.5;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}
.empty-text {
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
