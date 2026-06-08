<template>
  <div>
    <ProfileSectionHeader :title="$t('profile.projects.title')" :purpose="$t('profile.projects.purpose')" />
    <EmptyRecordsState :title="$t('profile.projects.emptyTitle')" :hint="$t('profile.projects.emptyHint')" />
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
