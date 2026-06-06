<template>
  <div>
    <h1 class="page-h1">{{ t('home.title') }}</h1>
    <div class="guided-block">
      <h2>{{ t('home.incomplete.title') }}</h2>
      <p>{{ t('home.incomplete.text') }}</p>
      <div class="guided-actions">
        <p-button :label="t('home.incomplete.cta')" icon="pi pi-user-edit" class="p-button-success" @click="$router.push('/profile/contact')" />
      </div>
      <ul class="inline-checklist" style="margin-top:16px">
        <li @click="$router.push('/profile/contact')">
          <span class="status-dot" :class="profileData.contactComplete ? 'done' : 'missing'" />
          <span>{{ t('home.checklist.contact') }}</span>
          <span class="status-text" :class="profileData.contactComplete ? 'done' : 'missing'">{{ profileData.contactComplete ? t('home.checklist.done') : t('home.checklist.missing') }}</span>
        </li>
        <li @click="$router.push('/profile/experience')">
          <span class="status-dot" :class="profileData.hasWorkExperience ? 'done' : 'missing'" />
          <span>{{ t('home.checklist.experience') }}</span>
          <span class="status-text" :class="profileData.hasWorkExperience ? 'done' : 'missing'">{{ profileData.hasWorkExperience ? t('home.checklist.done') : t('home.checklist.missing') }}</span>
        </li>
        <li @click="$router.push('/profile/education')">
          <span class="status-dot" :class="profileData.hasEducation ? 'done' : 'missing'" />
          <span>{{ t('home.checklist.education') }}</span>
          <span class="status-text" :class="profileData.hasEducation ? 'done' : 'missing'">{{ profileData.hasEducation ? t('home.checklist.done') : t('home.checklist.missing') }}</span>
        </li>
      </ul>
    </div>
    <div class="summary-cards">
      <div class="summary-card">
        <div class="card-label">{{ t('home.summary.savedResumes') }}</div>
        <div class="card-value">0</div>
        <div class="card-hint">{{ t('home.summary.incompleteResumesHint') }}</div>
      </div>
      <div class="summary-card">
        <div class="card-label">{{ t('home.summary.profileStatus') }}</div>
        <div class="card-value needs-info">{{ t('home.summary.needsInfo') }}</div>
        <div class="card-hint">{{ t('home.summary.needsInfoHint') }}</div>
        <a class="card-link" @click="$router.push('/profile/contact')"><i class="pi pi-pencil"></i> {{ t('home.summary.completeProfile') }}</a>
      </div>
      <div class="summary-card">
        <div class="card-label">{{ t('home.summary.lastResume') }}</div>
        <div class="card-value" style="font-size:1rem;font-weight:600">{{ t('home.summary.noLastResume') }}</div>
      </div>
    </div>
    <div class="section-header"><h2>{{ t('home.table.title') }}</h2></div>
    <div class="empty-state">
      <div class="empty-icon"><i class="pi pi-file"></i></div>
      <h3>{{ t('home.table.emptyTitle') }}</h3>
      <p>{{ t('home.incomplete.text') }}</p>
      <p-button :label="t('home.incomplete.cta')" icon="pi pi-user-edit" class="p-button-success" @click="$router.push('/profile/contact')" />
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'

const t = inject('t')
const profileData = ref({ contactComplete: false, hasWorkExperience: false, hasEducation: false })
</script>
