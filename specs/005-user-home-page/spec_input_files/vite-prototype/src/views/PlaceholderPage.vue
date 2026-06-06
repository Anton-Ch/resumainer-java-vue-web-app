<template>
  <div>
    <template v-if="isProfileRoute">
      <h1 class="page-h1">{{ t('nav.myProfile') }}</h1>
      <div class="profile-subnav">
        <router-link v-for="item in profileLinks" :key="item.route" :to="item.route">{{ t(item.key) }}</router-link>
      </div>
      <div class="guided-block" style="text-align:center;padding:48px 24px">
        <i class="pi pi-building" style="font-size:3rem;color:var(--muted-light);margin-bottom:16px;display:block" />
        <p style="color:var(--muted);font-size:1rem;margin-bottom:24px">{{ t('placeholder.text') }}</p>
        <p-button :label="t('placeholder.back')" icon="pi pi-arrow-left" class="p-button-success p-button-outlined" @click="$router.push('/demo/home-ready')" />
      </div>
    </template>
    <template v-else>
      <h1 class="page-h1">{{ t('placeholder.title') }}</h1>
      <div class="guided-block" style="text-align:center;padding:48px 24px">
        <i class="pi pi-building" style="font-size:3rem;color:var(--muted-light);margin-bottom:16px;display:block" />
        <p style="color:var(--muted);font-size:1rem;margin-bottom:24px">{{ t('placeholder.text') }}</p>
        <p-button :label="t('placeholder.back')" icon="pi pi-arrow-left" class="p-button-success p-button-outlined" @click="$router.push('/demo/home-ready')" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { inject, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const t = inject('t')
const lang = inject('lang')

const isProfileRoute = computed(() => route.path.startsWith('/profile/'))

const profileLinks = [
  { route: '/profile/contact', key: 'profile.subnav.contact' },
  { route: '/profile/experience', key: 'profile.subnav.experience' },
  { route: '/profile/education', key: 'profile.subnav.education' },
  { route: '/profile/projects', key: 'profile.subnav.projects' },
  { route: '/profile/courses', key: 'profile.subnav.courses' },
  { route: '/profile/additional', key: 'profile.subnav.additional' }
]
</script>
