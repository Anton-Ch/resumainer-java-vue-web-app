<template>
  <div class="summary-cards">
    <!-- Card 1: Saved resumes -->
    <div class="summary-card">
      <div class="card-label">{{ $t('home.summary.savedResumes') }}</div>
      <div class="card-value">{{ savedResumesCount }}</div>
      <div class="card-hint">
        <template v-if="profileReady">{{ $t('home.summary.readyHint') }}</template>
        <template v-else>{{ $t('home.summary.incompleteResumesHint') }}</template>
      </div>
    </div>

    <!-- Card 2: Profile status -->
    <div class="summary-card">
      <div class="card-label">{{ $t('home.summary.profileStatus') }}</div>
      <div class="card-value" :class="{ 'needs-info': !profileReady }">
        {{ profileReady ? $t('home.summary.ready') : $t('home.summary.needsInfo') }}
      </div>
      <div class="card-hint">
        <template v-if="profileReady">{{ $t('home.summary.readyStatusHint') }}</template>
        <template v-else>{{ $t('home.summary.needsInfoHint') }}</template>
      </div>
      <router-link v-if="profileReady" to="/profile/contact" class="card-link">
        <i class="pi pi-pencil"></i> {{ $t('home.summary.updateProfile') }}
      </router-link>
      <router-link v-else to="/profile/contact" class="card-link">
        <i class="pi pi-pencil"></i> {{ $t('home.summary.completeProfile') }}
      </router-link>
    </div>

    <!-- Card 3: Last resume -->
    <div class="summary-card" :class="{ clickable: !!lastResume }" @click="onLastResumeClick">
      <div class="card-label">{{ $t('home.summary.lastResume') }}</div>
      <template v-if="lastResume">
        <div class="card-value last-resume-title">{{ lastResume.resumeTitle }}</div>
        <div class="card-hint">{{ lastResume.createdAt }}</div>
      </template>
      <template v-else>
        <div class="card-value" style="font-size:1rem;font-weight:600">{{ $t('home.summary.noLastResume') }}</div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SavedResumeData } from '@/services/userHomeService'

const props = defineProps<{
  savedResumesCount: number
  profileReady: boolean
  lastResume: SavedResumeData | null
}>()

const emit = defineEmits<{
  openLastResume: []
}>()

function onLastResumeClick() {
  if (props.lastResume) {
    emit('openLastResume')
  }
}
</script>

<style scoped>
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}
.summary-card {
  background: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.summary-card.clickable {
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.summary-card.clickable:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.card-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.card-value {
  font-family: 'Manrope', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10233F;
  line-height: 1.2;
}
.card-value.needs-info {
  color: #D97706;
}
.card-value.last-resume-title {
  font-size: 0.9rem;
  font-weight: 600;
  font-family: var(--font-body, Inter, sans-serif);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-hint {
  font-size: 0.8rem;
  color: #5D718B;
}
.card-link {
  font-size: 0.8rem;
  color: #2F6BFF;
  text-decoration: none;
  margin-top: 0.25rem;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}
.card-link:hover {
  text-decoration: underline;
}
@media (max-width: 639px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }
}
</style>
