<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <nav class="profile-subnav">
        <router-link to="/profile/contact" active-class="router-link-exact-active">{{ $t('profile.subnav.contact') }}</router-link>
        <router-link to="/profile/experience" active-class="router-link-exact-active">{{ $t('profile.subnav.experience') }}</router-link>
        <router-link to="/profile/education" active-class="router-link-exact-active">{{ $t('profile.subnav.education') }}</router-link>
        <router-link to="/profile/projects" active-class="router-link-exact-active">{{ $t('profile.subnav.projects') }}</router-link>
        <router-link to="/profile/courses" active-class="router-link-exact-active">{{ $t('profile.subnav.courses') }}</router-link>
        <router-link to="/profile/additional" active-class="router-link-exact-active">{{ $t('profile.subnav.additional') }}</router-link>
      </nav>
      <div class="placeholder-content">
        <i class="pi pi-building" style="font-size: 3rem; color: #8091A7; margin-bottom: 1rem;"></i>
        <h1>{{ $t('placeholder.title') }}</h1>
        <p class="placeholder-text">{{ $t('placeholder.text') }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.main-content {
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 36px 32px;
}
.profile-subnav {
  display: flex;
  gap: 4px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  justify-content: center;
  background: #fff;
  border: 1px solid #E2E6EE;
  border-radius: 12px;
  padding: 6px;
  box-shadow: 0 1px 3px rgba(16,35,63,0.06);
}
.profile-subnav a {
  padding: 8px 18px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #5D718B;
  transition: all 0.15s;
  text-decoration: none;
  text-align: center;
}
.profile-subnav a:hover {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.profile-subnav a.router-link-exact-active {
  background: #0F9D7A;
  color: #fff;
}
.placeholder-content {
  text-align: center;
  padding: 48px 24px;
  background: #fff;
  border: 1px solid #E2E6EE;
  border-radius: 12px;
}
.placeholder-text {
  color: #5D718B;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}
@media (max-width: 640px) {
  .main-content {
    padding: 20px 16px;
  }
  .profile-subnav {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    justify-items: center;
    gap: 4px;
  }
  .profile-subnav a {
    width: 100%;
    text-align: center;
    font-size: 0.78rem;
    padding: 6px 4px;
    white-space: nowrap;
    box-sizing: border-box;
  }
}
</style>
