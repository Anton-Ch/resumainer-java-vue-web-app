<template>
  <div class="guided-block">
    <!-- Incomplete profile state -->
    <template v-if="!profileReady">
      <h2 class="guided-title">{{ $t('home.incomplete.title') }}</h2>
      <p class="guided-text">{{ $t('home.incomplete.text') }}</p>
      <div class="guided-cta">
        <Button :label="$t('home.incomplete.cta')" icon="pi pi-user-edit" class="p-button-success" @click="$router.push('/app/profile/contact')" />
      </div>
      <ProfileChecklistComponent :checklist="checklist" />
    </template>

    <!-- Ready profile state -->
    <template v-else>
      <h2 class="guided-title">{{ $t('home.ready.title') }}</h2>
      <div class="guided-cards">
        <div class="guided-card primary">
          <div class="card-body">
            <h3>{{ $t('home.ready.generate.title') }}</h3>
            <p>{{ $t('home.ready.generate.hint') }}</p>
          </div>
          <Button
            :label="$t('home.ready.generate.cta')"
            icon="pi pi-file-plus"
            class="p-button-success"
            v-tooltip.top="$t('home.ready.generate.tooltip')"
            @click="$router.push('/app/generate/vacancy')"
          />
        </div>
        <div class="guided-card">
          <div class="card-body">
            <h3>{{ $t('home.ready.update.title') }}</h3>
            <p>{{ $t('home.ready.update.hint') }}</p>
          </div>
          <Button
            :label="$t('home.ready.update.cta')"
            icon="pi pi-user-edit"
            class="p-button-success p-button-outlined"
            v-tooltip.top="$t('home.ready.update.tooltip')"
            @click="$router.push('/app/profile/contact')"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { ProfileChecklist as ProfileChecklistType } from '@/services/userHomeService'
import ProfileChecklistComponent from '@/components/home/ProfileChecklist.vue'
import Button from 'primevue/button'

defineProps<{
  profileReady: boolean
  checklist: ProfileChecklistType
}>()
</script>

<style scoped>
.guided-block {
  background: #FBFCFE;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  padding: 1.5rem;
}
.guided-title {
  font-family: 'Manrope', sans-serif;
  font-size: 1.15rem;
  font-weight: 700;
  color: #10233F;
  margin: 0 0 0.5rem;
}
.guided-text {
  color: #5D718B;
  font-size: 0.9rem;
  margin: 0 0 1rem;
  line-height: 1.5;
}
.guided-cta {
  margin-bottom: 0.5rem;
}
.guided-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-top: 1rem;
}
.guided-card {
  background: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.guided-card.primary {
  border-color: #0F9D7A;
  background: #F2FFF9;
}
.card-body h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.card-body p {
  font-size: 0.85rem;
  color: #5D718B;
  margin: 0;
  line-height: 1.4;
}
@media (max-width: 639px) {
  .guided-cards {
    grid-template-columns: 1fr;
  }
}
</style>
