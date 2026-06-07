<template>
  <div class="page">
    <AppHeader />
    <ProfileShell :sections="sectionStatuses" :activeSection="currentSection">
      <ContactDetailsSection
        v-if="currentSection === 'contact'"
        ref="contactRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('contact', $event)"
      />
      <WorkExperienceSection
        v-else-if="currentSection === 'experience'"
        ref="experienceRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('experience', $event)"
      />
      <ProjectsSection
        v-else-if="currentSection === 'projects'"
        ref="projectsRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('projects', $event)"
      />
      <EducationSection
        v-else-if="currentSection === 'education'"
        ref="educationRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('education', $event)"
      />
      <CoursesSection
        v-else-if="currentSection === 'courses'"
        ref="coursesRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('courses', $event)"
      />
      <AdditionalInfoSection
        v-else-if="currentSection === 'additional'"
        ref="additionalRef"
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import ProfileShell from '@/components/profile/ProfileShell.vue'
import ContactDetailsSection from '@/components/profile/sections/ContactDetailsSection.vue'
import WorkExperienceSection from '@/components/profile/sections/WorkExperienceSection.vue'
import ProjectsSection from '@/components/profile/sections/ProjectsSection.vue'
import EducationSection from '@/components/profile/sections/EducationSection.vue'
import CoursesSection from '@/components/profile/sections/CoursesSection.vue'
import AdditionalInfoSection from '@/components/profile/sections/AdditionalInfoSection.vue'
import UnsavedChangesDialog from '@/components/profile/UnsavedChangesDialog.vue'
import { getProfileSectionStatuses, seedSampleData } from '@/services/profileMockService'
import type { SectionStatus } from '@/types/profile'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const contactRef = ref<InstanceType<typeof ContactDetailsSection> | null>(null)
const experienceRef = ref<InstanceType<typeof WorkExperienceSection> | null>(null)
const projectsRef = ref<InstanceType<typeof ProjectsSection> | null>(null)
const educationRef = ref<InstanceType<typeof EducationSection> | null>(null)
const coursesRef = ref<InstanceType<typeof CoursesSection> | null>(null)
const additionalRef = ref<InstanceType<typeof AdditionalInfoSection> | null>(null)

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

const sectionStatuses = computed<SectionStatus[]>(() => {
  const statuses = getProfileSectionStatuses()
  return [
    {
      key: 'contact',
      label: t('profile.subnav.contact'),
      route: '/profile/contact',
      statusText: statuses.contactComplete ? t('profile.status.completed') : t('profile.status.incomplete'),
      statusType: statuses.contactComplete ? 'completed' : 'incomplete'
    },
    {
      key: 'experience',
      label: t('profile.subnav.experience'),
      route: '/profile/experience',
      statusText: formatCount(statuses.experienceCount),
      statusType: statuses.experienceCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'projects',
      label: t('profile.subnav.projects'),
      route: '/profile/projects',
      statusText: statuses.projectsCount > 0 ? formatCount(statuses.projectsCount) : t('profile.status.noRecords'),
      statusType: statuses.projectsCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'education',
      label: t('profile.subnav.education'),
      route: '/profile/education',
      statusText: formatCount(statuses.educationCount),
      statusType: statuses.educationCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'courses',
      label: t('profile.subnav.courses'),
      route: '/profile/courses',
      statusText: statuses.coursesCount > 0 ? formatCount(statuses.coursesCount) : t('profile.status.noRecords'),
      statusType: statuses.coursesCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'additional',
      label: t('profile.subnav.additional'),
      route: '/profile/additional',
      statusText: statuses.additionalComplete ? t('profile.status.completed') : t('profile.status.incomplete'),
      statusType: statuses.additionalComplete ? 'completed' : 'incomplete'
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

function refreshStatuses() {
  // reactivity handles computed recompute
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

onBeforeRouteUpdate((to, from, next) => {
  if (hasUnsavedChanges()) {
    unsavedDialogVisible.value = true
    pendingRoute.value = to.fullPath
    next(false)
  } else {
    next()
  }
})

function confirmLeave() {
  dirtyMap.value = {}
  if (pendingRoute.value) {
    router.push(pendingRoute.value)
    pendingRoute.value = null
  }
}

function cancelStay() {
  pendingRoute.value = null
}

// Seed sample data on first visit
onMounted(() => {
  seedSampleData()
})

// Guard against browser refresh
window.addEventListener('beforeunload', (e) => {
  if (hasUnsavedChanges()) {
    e.preventDefault()
  }
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
