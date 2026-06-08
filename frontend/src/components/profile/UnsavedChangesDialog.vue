<template>
  <Dialog
    v-model:visible="localVisible"
    modal
    :header="$t('profile.unsaved.title')"
    :style="{ maxWidth: '420px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
  >
    <p style="color: var(--vue-text-secondary); margin: 0;">{{ $t('profile.unsaved.message') }}</p>
    <template #footer>
      <div class="dialog-footer">
        <Button :label="$t('profile.unsaved.leave')" class="p-button-text" @click="onLeave" />
        <Button :label="$t('profile.unsaved.stay')" class="p-button-success" @click="onStay" autofocus />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirmLeave: []
  cancelStay: []
}>()

const localVisible = ref(props.visible)

watch(() => props.visible, (v) => {
  localVisible.value = v
})

watch(localVisible, (v) => {
  emit('update:visible', v)
})

function onLeave() {
  localVisible.value = false
  emit('confirmLeave')
}

function onStay() {
  localVisible.value = false
  emit('cancelStay')
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
