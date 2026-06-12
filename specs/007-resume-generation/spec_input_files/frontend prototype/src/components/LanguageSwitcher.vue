<template>
  <div class="vue-lang-switch" role="group" :aria-label="$t('language.en')">
    <button
      :class="{ active: currentLocale === 'en' }"
      :aria-label="$t('language.en')"
      :aria-pressed="currentLocale === 'en'"
      @click="setLocale('en')"
    >
      {{ $t('language.en') }}
    </button>
    <button
      :class="{ active: currentLocale === 'ru' }"
      :aria-label="$t('language.ru')"
      :aria-pressed="currentLocale === 'ru'"
      @click="setLocale('ru')"
    >
      {{ $t('language.ru') }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

function setLocale(loc: string) {
  locale.value = loc
  localStorage.setItem('locale', loc)
}
</script>

<style scoped>
.vue-lang-switch {
  display: inline-flex;
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 600;
  border: 1px solid var(--vue-border-soft);
  border-radius: 6px;
  overflow: hidden;
}
.vue-lang-switch button {
  padding: 4px 10px;
  height: 30px;
  background: transparent;
  border: none;
  color: var(--vue-text-muted);
  cursor: pointer;
  font-weight: 600;
  font-size: var(--vue-text-sm);
  transition: background var(--vue-motion-fast) var(--vue-ease-standard),
              color var(--vue-motion-fast) var(--vue-ease-standard);
}
.vue-lang-switch button:hover {
  color: var(--vue-text-primary);
}
.vue-lang-switch button.active {
  background: var(--vue-accent-blue);
  color: var(--vue-text-inverse);
}
</style>
