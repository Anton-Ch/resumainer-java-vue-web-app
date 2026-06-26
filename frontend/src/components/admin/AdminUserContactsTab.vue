<template>
  <div class="contacts-tab">
    <div class="detail-grid">
      <div class="detail-field">
        <span class="detail-label">{{ $t('admin.userDetails.fullName') }}</span>
        <span class="detail-value">{{ contacts?.fullName || $t('admin.userDetails.notProvided') }}</span>
      </div>
      <div class="detail-field">
        <span class="detail-label">{{ $t('admin.userDetails.resumeEmailLabel') }}</span>
        <span class="detail-value">{{ contacts?.resumeEmail || $t('admin.userDetails.notProvided') }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.professionalTitle">
        <span class="detail-label">{{ $t('admin.userDetails.professionalTitle') }}</span>
        <span class="detail-value">{{ contacts.professionalTitle }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.phone">
        <span class="detail-label">{{ $t('admin.userDetails.phone') }}</span>
        <span class="detail-value">{{ contacts.phone }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.location">
        <span class="detail-label">{{ $t('admin.userDetails.location') }}</span>
        <span class="detail-value">{{ contacts.location }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.linkedinUrl">
        <span class="detail-label">{{ $t('admin.userDetails.linkedinUrl') }}</span>
        <span class="detail-value link-value">{{ contacts.linkedinUrl }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.portfolioUrl">
        <span class="detail-label">{{ $t('admin.userDetails.portfolioUrl') }}</span>
        <span class="detail-value link-value">{{ contacts.portfolioUrl }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.telegram">
        <span class="detail-label">{{ $t('admin.userDetails.telegram') }}</span>
        <span class="detail-value">{{ contacts.telegram }}</span>
      </div>
      <div class="detail-field" v-if="contacts?.whatsapp">
        <span class="detail-label">{{ $t('admin.userDetails.whatsapp') }}</span>
        <span class="detail-value">{{ contacts.whatsapp }}</span>
      </div>
    </div>
    <p v-if="!contacts" class="empty-note">{{ $t('admin.userDetails.noContacts') }}</p>
  </div>
</template>

<script setup lang="ts">
import type { AdminUserContacts } from '@/types/admin'

defineProps<{
  contacts: AdminUserContacts | null
}>()
</script>

<style scoped>
.contacts-tab {
  max-width: 600px;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
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
}
.link-value {
  font-size: 0.8rem;
  color: #2F6BFF;
  word-break: break-all;
}
.empty-note {
  color: #5D718B;
  font-size: 0.9rem;
  font-style: italic;
}
@media (max-width: 480px) {
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
