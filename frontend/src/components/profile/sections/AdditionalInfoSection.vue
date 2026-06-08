<template>
  <div>
    <ProfileSectionHeader :title="$t('profile.additional.title')" />
    <div class="stub-placeholder">
      <i class="pi pi-spin pi-spinner" style="font-size: 1.5rem; color: var(--vue-text-muted);"></i>
      <p>{{ $t('placeholder.text') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import ProfileSectionHeader from '@/components/profile/ProfileSectionHeader.vue'

defineEmits<{
  saved: []
  'dirty-change': [dirty: boolean]
}>()
</script>

<style scoped>
.stub-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-lg);
  gap: 16px;
  color: var(--vue-text-muted);
}
</style>
