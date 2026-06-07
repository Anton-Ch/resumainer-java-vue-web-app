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
      <div class="detail-grid">
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.resumeTitle') }}</div>
          <div class="detail-value">{{ resume.resumeTitle }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.vacancy') }}</div>
          <div class="detail-value">{{ resume.vacancy }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.company') }}</div>
          <div class="detail-value">{{ resume.company }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.language') }}</div>
          <div class="detail-value">{{ resume.language === 'EN' ? $t('language.en') : $t('language.ru') }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.adaptationLevel') }}</div>
          <div class="detail-value">{{ $t('adaptation.' + resume.adaptationLevel.toLowerCase()) }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.created') }}</div>
          <div class="detail-value">{{ resume.createdAt }}</div>
        </div>
      </div>

      <div class="public-link-row">
        <i class="pi pi-link" style="color: #8091A7; font-size: 0.9rem;"></i>
        <span class="link-text">{{ resume.publicUrl }}</span>
        <Button :label="$t('resumeDetails.copyLink')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyLink" />
      </div>

      <Accordion :activeIndex="coverLetterOpen ? 0 : undefined">
        <AccordionTab :header="$t('resumeDetails.coverLetter')">
          <template v-if="resume.coverLetter">
            <p class="cover-letter-text">{{ resume.coverLetter }}</p>
            <Button :label="$t('resumeDetails.copyCoverLetter')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyCoverLetter" />
          </template>
          <p v-else style="color: #5D718B; font-size: 0.9rem;">{{ $t('resumeDetails.noCoverLetter') }}</p>
        </AccordionTab>
      </Accordion>

      <div class="modal-actions">
        <Button :label="$t('resumeDetails.view')" icon="pi pi-external-link" class="p-button-success" @click="viewResume" />
        <Button :label="$t('resumeDetails.downloadPdf')" icon="pi pi-download" class="p-button-success p-button-outlined" @click="downloadPdf" />
        <Button :label="$t('resumeDetails.delete')" icon="pi pi-trash" class="p-button-danger p-button-outlined" style="margin-left: auto;" @click="confirmDelete" />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
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
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  delete: [id: number]
}>()

const { t } = useI18n()
const confirm = useConfirm()
const toast = useToast()

const coverLetterOpen = ref(false)
const visible = ref(props.visible)

async function onCopyLink() {
  if (!props.resume?.publicUrl) return
  try {
    await navigator.clipboard.writeText(props.resume.publicUrl)
    toast.add({ severity: 'success', summary: '', detail: t('resumeDetails.linkCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: 'Failed to copy', life: 3000 })
  }
}

async function onCopyCoverLetter() {
  if (!props.resume?.coverLetter) return
  try {
    await navigator.clipboard.writeText(props.resume.coverLetter)
    toast.add({ severity: 'success', summary: '', detail: t('resumeDetails.coverLetterCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: 'Failed to copy', life: 3000 })
  }
}

function viewResume() {
  if (props.resume?.pdfUrl) {
    window.open(props.resume.pdfUrl, '_blank')
  }
}

function downloadPdf() {
  if (props.resume?.pdfUrl) {
    const a = document.createElement('a')
    a.href = props.resume.pdfUrl
    a.download = `${props.resume.resumeTitle || 'resume'}.pdf`
    a.click()
  }
}

function confirmDelete() {
  confirm.require({
    header: t('deleteResume.title'),
    message: t('deleteResume.text'),
    rejectLabel: t('deleteResume.cancel'),
    acceptLabel: t('deleteResume.confirm'),
    accept: () => {
      if (props.resume) {
        emit('delete', props.resume.id)
      }
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
  font-size: 0.85rem;
  color: #5D718B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cover-letter-text {
  font-size: 0.85rem;
  color: #374151;
  line-height: 1.5;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}
.modal-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.25rem;
  padding-top: 1rem;
  border-top: 1px solid #E5E7EB;
}
@media (max-width: 480px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
