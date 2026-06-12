<template>
  <div class="review-form">
    <div v-if="!reviewData" class="empty-state">
      <p>{{ $t('generate.review.noData') }}</p>
    </div>

    <template v-else>
      <!-- Adaptation level selection for finalize -->
      <div class="finalize-section">
        <h3>{{ $t('generate.review.selectLevel') }}</h3>
        <AdaptationLevelRadioGroup v-model="selectedLevel" />
        <Button :label="$t('generate.review.finalize')" icon="pi pi-check" @click="onFinalize" :disabled="!selectedLevel" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdaptationLevelRadioGroup from './AdaptationLevelRadioGroup.vue'

defineProps<{ reviewData: any }>()
const emit = defineEmits<{ (e: 'finalize', level: string): void }>()

const selectedLevel = ref('BALANCED')

function onFinalize() {
  emit('finalize', selectedLevel.value)
}
</script>
