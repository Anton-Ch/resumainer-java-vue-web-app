<template>
  <p-dialog :visible="modelValue" :header="t('resumeDetails.title')" :modal="true" :style="{ maxWidth: '580px', width: '100%' }" :dismissableMask="true" :closable="true" :closeOnEscape="true" @update:visible="onHide">
    <template v-if="resume">
      <div class="resume-detail-grid">
        <div><div class="detail-label">{{ t('home.table.resumeTitle') }}</div><div class="detail-value">{{ resume.title }}</div></div>
        <div><div class="detail-label">{{ t('home.table.vacancy') }}</div><div class="detail-value">{{ resume.vacancy }}</div></div>
        <div><div class="detail-label">{{ t('home.table.company') }}</div><div class="detail-value">{{ resume.company }}</div></div>
        <div><div class="detail-label">{{ t('home.table.language') }}</div><div class="detail-value">{{ t(resume.language === 'English' ? 'language.en' : 'language.ru') }}</div></div>
        <div><div class="detail-label">{{ t('home.table.adaptationLevel') }}</div><div class="detail-value">{{ t('adaptation.'+resume.adaptationLevel.toLowerCase()) }}</div></div>
        <div><div class="detail-label">{{ t('home.table.created') }}</div><div class="detail-value">{{ resume.created }}</div></div>
      </div>
      <div class="public-link-row">
        <i class="pi pi-link" style="color:var(--muted-light);font-size:0.9rem"></i>
        <span class="link-text">{{ resume.publicLink }}</span>
        <p-button :label="t('resumeDetails.copyLink')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyLink" />
      </div>
      <p-accordion :activeIndex="coverLetterOpen ? 0 : undefined">
        <p-accordion-tab :header="t('resumeDetails.coverLetter')">
          <div class="cover-letter-block">
            <template v-if="resume.coverLetter">
              <div class="cover-letter-preview">{{ resume.coverLetter }}</div>
              <p-button :label="t('resumeDetails.copyCoverLetter')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyCoverLetter" style="margin-top:8px" />
            </template>
            <p v-else style="color:var(--muted);font-size:0.9rem">{{ t('resumeDetails.noCoverLetter') }}</p>
          </div>
        </p-accordion-tab>
      </p-accordion>
      <div class="modal-actions">
        <p-button :label="t('resumeDetails.view')" icon="pi pi-external-link" class="p-button-success" @click="viewResume" />
        <p-button :label="t('resumeDetails.downloadPdf')" icon="pi pi-download" class="p-button-success p-button-outlined" @click="downloadPdf" />
        <p-button :label="t('resumeDetails.delete')" icon="pi pi-trash" class="p-button-danger p-button-outlined" style="margin-left:auto" @click="onDelete" />
      </div>
    </template>
  </p-dialog>
</template>

<script setup>
import { ref, inject } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  resume: Object
})
const emit = defineEmits(['update:modelValue', 'delete'])

const onHide = (val) => { emit('update:modelValue', val) }

const t = inject('t')
const lang = inject('lang')
const showToast = inject('showToast')
const coverLetterOpen = ref(false)

const onCopyLink = async () => {
  if (!props.resume) return
  try { await navigator.clipboard.writeText(props.resume.publicLink); showToast('success', t('resumeDetails.linkCopied')) }
  catch { showToast('error', lang.value === 'ru' ? 'Не удалось скопировать' : 'Failed to copy') }
}

const onCopyCoverLetter = async () => {
  if (!props.resume?.coverLetter) return
  try { await navigator.clipboard.writeText(props.resume.coverLetter); showToast('success', t('resumeDetails.coverLetterCopied')) }
  catch { showToast('error', lang.value === 'ru' ? 'Не удалось скопировать' : 'Failed to copy') }
}

const viewResume = () => {
  if (props.resume) window.open(props.resume.publicLink, '_blank')
}

const downloadPdf = () => {
  showToast('success', lang.value === 'ru' ? 'PDF скачивается...' : 'Downloading PDF...')
}

const onDelete = () => {
  if (props.resume) emit('delete', props.resume)
}
</script>
