<template>
  <div>
    <h1 class="page-h1">{{ t('home.title') }}</h1>
    <div class="guided-block">
      <p-skeleton width="40%" height="24px" borderRadius="6px" />
      <p-skeleton width="70%" height="16px" style="margin-top:12px" />
      <div style="display:flex;gap:12px;margin-top:16px">
        <p-skeleton width="160px" height="36px" borderRadius="6px" />
        <p-skeleton width="140px" height="36px" borderRadius="6px" />
      </div>
    </div>
    <div class="summary-cards">
      <div v-for="i in 3" :key="i" class="summary-card">
        <p-skeleton width="60%" height="14px" borderRadius="4px" />
        <p-skeleton width="40%" height="28px" borderRadius="6px" style="margin-top:8px" />
        <p-skeleton width="80%" height="14px" borderRadius="4px" style="margin-top:8px" />
      </div>
    </div>
    <div class="section-header">
      <p-skeleton width="180px" height="20px" borderRadius="4px" />
      <p-skeleton width="140px" height="36px" borderRadius="6px" />
    </div>
    <p-datatable :value="[]" :loading="true">
      <p-column field="title" :header="t('home.table.resumeTitle')">
        <template #loading><p-skeleton width="70%" height="16px" borderRadius="4px" /></template>
      </p-column>
      <p-column field="vacancy" :header="t('home.table.vacancy')">
        <template #loading><p-skeleton width="60%" height="16px" borderRadius="4px" /></template>
      </p-column>
      <p-column field="company" :header="t('home.table.company')">
        <template #loading><p-skeleton width="50%" height="16px" borderRadius="4px" /></template>
      </p-column>
      <p-column field="language" :header="t('home.table.language')">
        <template #loading><p-skeleton width="60px" height="20px" borderRadius="4px" /></template>
      </p-column>
      <p-column field="adaptationLevel" :header="t('home.table.adaptationLevel')">
        <template #loading><p-skeleton width="70px" height="20px" borderRadius="4px" /></template>
      </p-column>
      <p-column field="created" :header="t('home.table.created')">
        <template #loading><p-skeleton width="80px" height="16px" borderRadius="4px" /></template>
      </p-column>
    </p-datatable>
  </div>
</template>

<script setup>
import { inject } from 'vue'

const t = inject('t')
</script>
