<template>
  <div class="record-group">
    <div v-for="record in records" :key="record.id" class="record-item">
      <div class="record-header">
        <span class="record-title">{{ record.jobTitle || record.projectName || record.name }}</span>
        <span v-if="record.companyName" class="record-subtitle">— {{ record.companyName }}</span>
      </div>
      <p v-if="record.description" class="record-desc">{{ record.description }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ records: any[] }>()
</script>
