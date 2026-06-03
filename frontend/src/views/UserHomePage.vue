<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <!-- Stats overview -->
      <div class="stats-row">
        <div class="stat-card vue-card">
          <span class="stat-label">{{ $t('home.totalTokensSent') }}</span>
          <span class="stat-value">0</span>
        </div>
        <div class="stat-card vue-card">
          <span class="stat-label">{{ $t('home.totalTokensGenerated') }}</span>
          <span class="stat-value">0</span>
        </div>
        <div class="stat-card vue-card">
          <span class="stat-label">{{ $t('home.totalResumes') }}</span>
          <span class="stat-value">0</span>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="action-row">
        <button class="vue-btn vue-btn-secondary vue-btn-lg" @click="goToProfile">
          {{ $t('home.editProfile') }}
        </button>
        <button class="vue-btn vue-btn-primary vue-btn-lg" @click="goToGenerate">
          {{ $t('home.generateResume') }}
        </button>
      </div>

      <!-- Resume table placeholder (empty state) -->
      <div class="vue-card resume-section">
        <div class="vue-empty">
          <div class="vue-empty-icon">
            <svg viewBox="0 0 48 48" fill="none" aria-hidden="true">
              <rect x="6" y="4" width="36" height="40" rx="4" stroke="currentColor" stroke-width="2" fill="none"/>
              <line x1="14" y1="16" x2="34" y2="16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="14" y1="24" x2="34" y2="24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="14" y1="32" x2="28" y2="32" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="vue-empty-title">{{ $t('home.noResumes') }}</h3>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()

function goToProfile() {
  // Will be implemented in a future feature
}

function goToGenerate() {
  // Will be implemented in a future feature
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.page-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: var(--vue-space-6);
  display: flex;
  flex-direction: column;
  gap: var(--vue-space-6);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.action-row {
  display: flex;
  gap: var(--vue-space-4);
  flex-wrap: wrap;
}

.resume-section {
  flex: 1;
}

@media (max-width: 639px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .page-main {
    padding: var(--vue-space-4);
  }
}
</style>
