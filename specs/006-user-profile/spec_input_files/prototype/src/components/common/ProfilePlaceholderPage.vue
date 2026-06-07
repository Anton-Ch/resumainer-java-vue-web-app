<template>
  <div class="page">
    <AppHeader />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()

onMounted(() => {
  router.replace('/profile/contact')
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
</style>
