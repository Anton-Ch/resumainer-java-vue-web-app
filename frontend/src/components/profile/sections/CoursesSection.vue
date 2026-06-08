<template>
  <div>
    <ProfileSectionHeader :title="$t('profile.courses.title')" :purpose="$t('profile.courses.purpose')" />
    <EmptyRecordsState :title="$t('profile.courses.emptyTitle')" :hint="$t('profile.courses.emptyHint')" />
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
