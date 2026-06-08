<template>
  <div class="profile-shell">
    <div class="shell-layout">
      <ProfileSidebar
        v-if="!isMobile"
        :sections="sections"
        :activeSection="activeSection"
        class="shell-sidebar"
      />
      <ProfileMobileTabs
        v-if="isMobile"
        :sections="sections"
        :activeSection="activeSection"
      />
      <div class="shell-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { SidebarSection } from '@/types/profile'
import ProfileSidebar from './ProfileSidebar.vue'
import ProfileMobileTabs from './ProfileMobileTabs.vue'

defineProps<{
  sections: SidebarSection[]
  activeSection: string
}>()

const isMobile = ref(false)

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.profile-shell {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 32px;
}
.shell-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}
.shell-sidebar {
  position: sticky;
  top: 92px;
}
.shell-content {
  flex: 1;
  min-width: 0;
}
@media (max-width: 767px) {
  .profile-shell {
    padding: 16px;
  }
  .shell-layout {
    flex-direction: column;
    gap: 0;
  }
}
</style>
