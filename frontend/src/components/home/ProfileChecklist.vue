<template>
  <ul class="checklist">
    <li
      v-for="item in items"
      :key="item.key"
      class="checklist-item"
      @click="$router.push(item.route)"
    >
      <span class="status-dot" :class="item.done ? 'done' : 'missing'" />
      <span class="checklist-label">{{ item.label }}</span>
      <span class="status-text" :class="item.done ? 'done' : 'missing'">
        {{ item.done ? $t('home.checklist.done') : $t('home.checklist.missing') }}
      </span>
    </li>
  </ul>
</template>

<script setup lang="ts">
import type { ProfileChecklist } from '@/services/userHomeService'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  checklist: ProfileChecklist
}>()

const { t } = useI18n()

const items = computed(() => [
  {
    key: 'contact',
    label: t('home.checklist.contact'),
    done: props.checklist.contactDetails,
    route: '/profile/contact'
  },
  {
    key: 'experience',
    label: t('home.checklist.experience'),
    done: props.checklist.workExperience,
    route: '/profile/experience'
  },
  {
    key: 'education',
    label: t('home.checklist.education'),
    done: props.checklist.education,
    route: '/profile/education'
  },
  {
    key: 'additional',
    label: t('profile.subnav.additional'),
    done: props.checklist.additionalInfo,
    route: '/profile/additional'
  }
])
</script>

<style scoped>
.checklist {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
}
.checklist-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}
.checklist-item:hover {
  background: #F3F4F6;
}
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-dot.done {
  background: #0F9D7A;
}
.status-dot.missing {
  background: #D97706;
}
.checklist-label {
  flex: 1;
  font-size: 0.875rem;
  color: #10233F;
}
.status-text {
  font-size: 0.8rem;
  font-weight: 600;
}
.status-text.done {
  color: #0F9D7A;
}
.status-text.missing {
  color: #D97706;
}
</style>
