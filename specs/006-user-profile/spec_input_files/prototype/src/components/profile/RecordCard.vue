<template>
  <div class="record-card">
    <div class="record-card-header">
      <div class="record-card-title-row">
        <h4 class="record-card-title">{{ title }}</h4>
        <div class="record-card-actions">
          <Button
            :label="$t(editLabelKey)"
            icon="pi pi-pencil"
            class="p-button-text p-button-sm"
            @click="$emit('edit')"
          />
          <Button
            :label="$t(deleteLabelKey)"
            icon="pi pi-trash"
            class="p-button-text p-button-sm p-button-danger"
            @click="$emit('delete')"
          />
        </div>
      </div>
      <div class="record-card-meta">
        <span>{{ metaLine }}</span>
        <span v-if="chipLabel" class="vue-chip vue-chip-primary" style="margin-left: 8px;">{{ chipLabel }}</span>
      </div>
      <p v-if="description" class="record-card-desc">{{ description }}</p>
      <a v-if="url" :href="url" target="_blank" class="record-card-url" @click.stop>{{ url }}</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'

defineProps<{
  title: string
  metaLine: string
  description?: string
  url?: string
  chipLabel?: string
  editLabelKey?: string
  deleteLabelKey?: string
}>()

defineEmits<{
  edit: []
  delete: []
}>()
</script>

<style scoped>
.record-card {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-md);
  padding: 14px 16px;
  margin-bottom: 10px;
}
.record-card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.record-card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.record-card-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-base);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
  line-height: 1.3;
}
.record-card-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}
.record-card-meta {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.record-card-desc {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
  margin: 6px 0 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.record-card-url {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-accent-blue);
  margin-top: 4px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
</style>
