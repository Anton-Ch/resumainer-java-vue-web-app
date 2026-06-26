<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <div class="wip-content">
        <i class="pi pi-spin pi-cog" style="font-size: 2.5rem; color: var(--vue-accent-violet);"></i>

        <div class="wip-badge">{{ $t('admin.aiModels.wipBadge') }}</div>

        <h1>{{ $t('admin.aiModels.title') }}</h1>

        <div class="description">
          <p>{{ $t('admin.aiModels.description') }}</p>
          <p class="safety-note">{{ $t('admin.aiModels.safetyNote') }}</p>
        </div>

        <div class="planned-section">
          <h3>{{ $t('admin.aiModels.plannedTitle') }}</h3>
          <ul>
            <li v-for="item in plannedItems" :key="item">{{ item }}</li>
          </ul>
        </div>

        <div class="nav-actions">
          <router-link to="/admin">
            <Button :label="$t('admin.aiModels.backToHome')" icon="pi pi-home" class="p-button-text" />
          </router-link>
          <router-link to="/admin/users">
            <Button :label="$t('admin.aiModels.goToUsers')" icon="pi pi-users" class="p-button-text" />
          </router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import Button from 'primevue/button'

const { t } = useI18n()

const plannedItems = computed(() => [
  t('admin.aiModels.planModelList'),
  t('admin.aiModels.planProviderConfig'),
  t('admin.aiModels.planDefaultModel'),
  t('admin.aiModels.planUsageCost'),
])
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.page-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}
.wip-content {
  text-align: center;
  max-width: 520px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}
.wip-badge {
  display: inline-block;
  background: #FEF3C7;
  color: #92400E;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 9999px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.wip-content h1 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10233F;
  margin: 0;
}
.description {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.description p {
  margin: 0;
  color: #5D718B;
  font-size: 0.9rem;
  line-height: 1.5;
}
.safety-note {
  padding: 0.75rem;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  color: #92400E;
  font-size: 0.85rem;
}
.planned-section {
  text-align: left;
  width: 100%;
  padding: 1rem;
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
}
.planned-section h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 0.85rem;
  font-weight: 700;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 0.5rem;
}
.planned-section ul {
  margin: 0;
  padding-left: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.planned-section li {
  font-size: 0.85rem;
  color: #5D718B;
}
.nav-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: center;
}
</style>
