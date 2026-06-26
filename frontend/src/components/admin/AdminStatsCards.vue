<template>
  <div class="stats-row">
    <div class="vue-card stat-card">
      <span class="stat-label">{{ $t('admin.stats.totalUsers') }}</span>
      <span class="stat-value">{{ totalUsers }}</span>
    </div>
    <div class="vue-card stat-card">
      <span class="stat-label">{{ $t('admin.stats.totalResumes') }}</span>
      <span class="stat-value">{{ totalResumes }}</span>
    </div>
    <div class="vue-card stat-card">
      <span class="stat-label">{{ $t('admin.stats.totalTokensSent') }}</span>
      <span class="stat-value">{{ totalTokensSent }}</span>
      <span v-if="totalTokensSentWip" class="wip-badge">{{ $t('admin.stats.wip') }}</span>
    </div>
    <div class="vue-card stat-card">
      <span class="stat-label">{{ $t('admin.stats.totalTokensGenerated') }}</span>
      <span class="stat-value">{{ totalTokensGenerated }}</span>
      <span v-if="totalTokensGeneratedWip" class="wip-badge">{{ $t('admin.stats.wip') }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  totalUsers: number
  totalResumes: number
  totalTokensSent: number
  totalTokensSentWip: boolean
  totalTokensGenerated: number
  totalTokensGeneratedWip: boolean
}>()
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--vue-space-6);
}
.stat-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
  font-weight: 500;
}
.stat-value {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-2xl);
  font-weight: 700;
  color: var(--vue-text-primary);
}
.wip-badge {
  display: inline-block;
  background: #FEF3C7;
  color: #92400E;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 9999px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  align-self: flex-start;
}
@media (max-width: 639px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
@media (min-width: 640px) and (max-width: 1023px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
