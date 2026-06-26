<template>
  <div class="additional-tab">
    <div class="detail-grid" v-if="info">
      <div class="detail-field">
        <span class="detail-label">{{ $t('admin.userDetails.username') }}</span>
        <span class="detail-value">{{ username || $t('admin.userDetails.notProvided') }}</span>
      </div>
      <div class="detail-field" v-if="info.skills">
        <span class="detail-label">{{ $t('admin.userDetails.skills') }}</span>
        <span class="detail-value">{{ info.skills }}</span>
      </div>
      <div class="detail-field" v-if="info.languages">
        <span class="detail-label">{{ $t('admin.userDetails.spokenLanguages') }}</span>
        <span class="detail-value">{{ info.languages }}</span>
      </div>
      <div class="detail-field" v-if="info.professionalAspirations">
        <span class="detail-label">{{ $t('admin.userDetails.professionalAspirations') }}</span>
        <span class="detail-value">{{ info.professionalAspirations }}</span>
      </div>
      <div class="detail-field" v-if="info.achievements">
        <span class="detail-label">{{ $t('admin.userDetails.achievements') }}</span>
        <span class="detail-value">{{ info.achievements }}</span>
      </div>
      <div class="detail-field" v-if="info.generalInformation">
        <span class="detail-label">{{ $t('admin.userDetails.generalInformation') }}</span>
        <span class="detail-value">{{ info.generalInformation }}</span>
      </div>
      <div class="detail-field" v-if="info.readyForRelocation">
        <span class="detail-label">{{ $t('admin.userDetails.readyForRelocation') }}</span>
        <span class="detail-value">{{ info.readyForRelocation }}</span>
      </div>
      <div class="detail-field" v-if="info.readyForBusinessTrips">
        <span class="detail-label">{{ $t('admin.userDetails.readyForBusinessTrips') }}</span>
        <span class="detail-value">{{ info.readyForBusinessTrips }}</span>
      </div>
      <div class="detail-field" v-if="info.dateOfBirth">
        <span class="detail-label">{{ $t('admin.userDetails.dateOfBirth') }}</span>
        <span class="detail-value">{{ info.dateOfBirth }}</span>
      </div>
      <div class="detail-field" v-if="info.citizenship">
        <span class="detail-label">{{ $t('admin.userDetails.citizenship') }}</span>
        <span class="detail-value">{{ info.citizenship }}</span>
      </div>
    </div>
    <p v-if="!info" class="empty-note">{{ $t('admin.userDetails.noAdditionalInfo') }}</p>
  </div>
</template>

<script setup lang="ts">
import type { AdminUserAdditionalInfo } from '@/types/admin'

defineProps<{
  info: AdminUserAdditionalInfo | null
  username: string
}>()
</script>

<style scoped>
.additional-tab {
  max-width: 600px;
}
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.detail-field {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}
.detail-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.detail-value {
  font-size: 0.9rem;
  color: #10233F;
  word-break: break-word;
  white-space: pre-wrap;
}
.empty-note {
  color: #5D718B;
  font-size: 0.9rem;
  font-style: italic;
}
</style>
