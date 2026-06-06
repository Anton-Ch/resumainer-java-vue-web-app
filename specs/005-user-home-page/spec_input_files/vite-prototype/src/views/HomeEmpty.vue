<template>
  <div>
    <h1 class="page-h1">{{ t('home.title') }}</h1>
    <div class="guided-block">
      <h2>{{ t('home.ready.title') }}</h2>
      <p>{{ t('home.ready.generate.hint') }}</p>
      <div class="guided-cards">
        <div class="guided-card primary">
          <h3>{{ t('home.ready.generate.title') }}</h3>
          <p>{{ t('home.ready.generate.hint') }}</p>
          <p-button :label="t('home.ready.generate.cta')" icon="pi pi-file-plus" class="p-button-success" v-tooltip.top="t('home.ready.generate.tooltip')" @click="$router.push('/generate/vacancy')" />
        </div>
        <div class="guided-card">
          <h3>{{ t('home.ready.update.title') }}</h3>
          <p>{{ t('home.ready.update.hint') }}</p>
          <p-button :label="t('home.ready.update.cta')" icon="pi pi-user-edit" class="p-button-success p-button-outlined" v-tooltip.top="t('home.ready.update.tooltip')" @click="$router.push('/profile/contact')" />
        </div>
      </div>
    </div>
    <div class="summary-cards">
      <div class="summary-card">
        <div class="card-label">{{ t('home.summary.savedResumes') }}</div>
        <div class="card-value">0</div>
        <div class="card-hint">{{ t('home.summary.emptyResumesHint') }}</div>
      </div>
      <div class="summary-card">
        <div class="card-label">{{ t('home.summary.profileStatus') }}</div>
        <div class="card-value">{{ t('home.summary.ready') }}</div>
        <div class="card-hint">{{ t('home.summary.emptyStatusHint') }}</div>
        <a class="card-link" @click="$router.push('/profile/contact')"><i class="pi pi-pencil"></i> {{ t('home.summary.updateProfile') }}</a>
      </div>
      <div class="summary-card">
        <div class="card-label">{{ t('home.summary.lastResume') }}</div>
        <div class="card-value" style="font-size:1rem;font-weight:600">{{ t('home.summary.noLastResume') }}</div>
      </div>
    </div>
    <div class="section-header">
      <h2>{{ t('home.table.title') }}</h2>
      <p-button :label="t('home.ready.generate.cta')" icon="pi pi-plus" class="p-button-success p-button-outlined" v-tooltip.top="t('home.ready.generate.tooltip')" @click="$router.push('/generate/vacancy')" />
    </div>
    <div class="empty-state">
      <div class="empty-icon"><i class="pi pi-file"></i></div>
      <h3>{{ t('home.table.emptyTitle') }}</h3>
      <p>{{ t('home.incomplete.text') }}</p>
      <p-button :label="t('home.ready.generate.cta')" icon="pi pi-file-plus" class="p-button-success" @click="$router.push('/generate/vacancy')" />
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'

const t = inject('t')
</script>
