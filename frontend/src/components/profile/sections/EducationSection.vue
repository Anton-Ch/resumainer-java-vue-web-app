<template>
  <div>
    <ProfileSectionHeader :title="$t('profile.education.title')" :purpose="$t('profile.education.purpose')" />
    <EmptyRecordsState :title="$t('profile.education.emptyTitle')" :hint="$t('profile.education.emptyHint')" />
  </div>
</template>

<script setup lang="ts">
import ProfileSectionHeader from '@/components/profile/ProfileSectionHeader.vue'
import EmptyRecordsState from '@/components/profile/EmptyRecordsState.vue'

defineEmits<{
  saved: []
  'dirty-change': [dirty: boolean]
}>()
</script>
