<template>
  <div class="page">
    <AppHeader />
    <ProfileShell :sections="sectionStatuses" :activeSection="currentSection">
      <ContactDetailsSection
        v-if="currentSection === 'contact'"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('contact', $event)"
      />
      <WorkExperienceSection
        v-else-if="currentSection === 'experience'"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('experience', $event)"
      />
      <ProjectsSection
        v-else-if="currentSection === 'projects'"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('projects', $event)"
      />
      <EducationSection
        v-else-if="currentSection === 'education'"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('education', $event)"
      />
      <CoursesSection
        v-else-if="currentSection === 'courses'"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('courses', $event)"
      />
      <AdditionalInfoSection
        v-else-if="currentSection === 'additional'"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('additional', $event)"
      />
    </ProfileShell>
    <UnsavedChangesDialog
      v-model:visible="unsavedDialogVisible"
      @confirm-leave="confirmLeave"
      @cancel-stay="cancelStay"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import ProfileShell from '@/components/profile/ProfileShell.vue'
import UnsavedChangesDialog from '@/components/profile/UnsavedChangesDialog.vue'
import ContactDetailsSection from '@/components/profile/sections/ContactDetailsSection.vue'
import WorkExperienceSection from '@/components/profile/sections/WorkExperienceSection.vue'
import ProjectsSection from '@/components/profile/sections/ProjectsSection.vue'
import EducationSection from '@/components/profile/sections/EducationSection.vue'
import CoursesSection from '@/components/profile/sections/CoursesSection.vue'
import AdditionalInfoSection from '@/components/profile/sections/AdditionalInfoSection.vue'
import { fetchSectionStatus } from '@/services/profileService'
import type { ProfileSectionStatus, SidebarSection } from '@/types/profile'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const apiStatuses = ref<ProfileSectionStatus | null>(null)
const dirtyMap = ref<Record<string, boolean>>({})
const unsavedDialogVisible = ref(false)
const pendingRoute = ref<string | null>(null)

const currentSection = computed(() => {
  const path = route.path
  if (path.includes('/contact')) return 'contact'
  if (path.includes('/experience')) return 'experience'
  if (path.includes('/projects')) return 'projects'
  if (path.includes('/education')) return 'education'
  if (path.includes('/courses')) return 'courses'
  if (path.includes('/additional')) return 'additional'
  return 'contact'
})

const sectionStatuses = computed<SidebarSection[]>(() => {
  const s = apiStatuses.value
  if (!s) return []

  return [
    {
      key: 'contact',
      label: t('profile.subnav.contact'),
      route: '/profile/contact',
      statusText: s.contact === 'completed' ? t('profile.status.completed') : t('profile.status.incomplete'),
      statusType: s.contact === 'completed' ? 'completed' as const : 'incomplete' as const
    },
    {
      key: 'experience',
      label: t('profile.subnav.experience'),
      route: '/profile/experience',
      statusText: formatCount(s.experience.count),
      statusType: s.experience.count > 0 ? 'count' as const : 'no-records' as const
    },
    {
      key: 'education',
      label: t('profile.subnav.education'),
      route: '/profile/education',
      statusText: formatCount(s.education.count),
      statusType: s.education.count > 0 ? 'count' as const : 'no-records' as const
    },
    {
      key: 'projects',
      label: t('profile.subnav.projects'),
      route: '/profile/projects',
      statusText: formatCount(s.projects.count),
      statusType: s.projects.count > 0 ? 'count' as const : 'no-records' as const
    },
    {
      key: 'courses',
      label: t('profile.subnav.courses'),
      route: '/profile/courses',
      statusText: formatCount(s.courses.count),
      statusType: s.courses.count > 0 ? 'count' as const : 'no-records' as const
    },
    {
      key: 'additional',
      label: t('profile.subnav.additional'),
      route: '/profile/additional',
      statusText: s.additional === 'completed' ? t('profile.status.completed') : t('profile.status.incomplete'),
      statusType: s.additional === 'completed' ? 'completed' as const : 'incomplete' as const
    }
  ]
})

function formatCount(count: number): string {
  if (count === 1) return t('profile.status.record', { count })
  if (count === 0) return t('profile.status.noRecordsShort')
  return t('profile.status.records', { count })
}

function onDirtyChange(section: string, dirty: boolean) {
  dirtyMap.value[section] = dirty
}

function hasUnsavedChanges(): boolean {
  return Object.values(dirtyMap.value).some(v => v === true)
}

async function refreshStatuses() {
  try {
    apiStatuses.value = await fetchSectionStatus()
  } catch {
    // Silently fail — statuses will remain stale
  }
}

// Navigation guards for unsaved changes
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges()) {
    unsavedDialogVisible.value = true
    pendingRoute.value = to.fullPath
    next(false)
  } else {
    next()
  }
})

// Handle in-component navigation with dirty check
const unwatch = route.query
let unwatchRoute: (() => void) | null = null

function confirmLeave() {
  dirtyMap.value = {}
  unsavedDialogVisible.value = false
  if (pendingRoute.value) {
    router.push(pendingRoute.value)
    pendingRoute.value = null
  }
}

function cancelStay() {
  unsavedDialogVisible.value = false
  pendingRoute.value = null
}

onMounted(() => {
  refreshStatuses()
})

// Guard against browser refresh
function beforeUnload(e: BeforeUnloadEvent) {
  if (hasUnsavedChanges()) {
    e.preventDefault()
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', beforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnload)
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--vue-bg-canvas);
}
</style>
