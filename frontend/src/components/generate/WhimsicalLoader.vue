<template>
  <div class="loader-overlay" role="dialog" aria-modal="true" :aria-label="title">
    <div class="loader-card">
      <div class="loader-spinner">
        <i class="pi pi-spin pi-spinner" style="font-size: 2.5rem; color: #0F9D7A;"></i>
      </div>
      <h3 class="loader-title">{{ title }}</h3>
      <p class="loader-phrase">{{ currentPhrase }}</p>
      <p class="loader-hint">{{ hint }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'

const EN_PHRASES = [
  'Reticulating splines...',
  'Assembling the Avengers...',
  'Bending time and space...',
  'Marinating data (letting them to infuse flavor)',
  'Reading your profile and admiring...',
  'Chillax... (while we are working)'
]

const RU_PHRASES = [
  'Натягиваем сову на глобус',
  'Стучимся в чат к Мстителям',
  'Включаем гипердрайв',
  'Маринуем данные (пусть настоятся для вкуса)',
  'Читаем твой профиль и восхищаемся им...',
  'Узбагойся... (пока мы работаем)'
]

const props = defineProps<{ title: string; hint: string }>()
const { locale } = useI18n()
const currentPhrase = ref('')
let intervalId: ReturnType<typeof setInterval> | null = null
let lastIndex = -1

function pickRandomPhrase(): string {
  const phrases = locale.value === 'ru' ? RU_PHRASES : EN_PHRASES
  let idx: number; do { idx = Math.floor(Math.random() * phrases.length) } while (idx === lastIndex && phrases.length > 1)
  lastIndex = idx; return phrases[idx]
}

function rotatePhrase() { currentPhrase.value = pickRandomPhrase() }

onMounted(() => { rotatePhrase(); intervalId = setInterval(rotatePhrase, 3000) })
onBeforeUnmount(() => { if (intervalId) { clearInterval(intervalId); intervalId = null } })
</script>

<style scoped>
.loader-overlay { position: fixed; inset: 0; background: rgba(22,33,43,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.loader-card { background: #fff; border-radius: 12px; padding: 40px 32px; max-width: 480px; width: 100%; text-align: center; box-shadow: 0 20px 60px rgba(16,35,63,0.14); }
.loader-spinner { margin-bottom: 20px; }
.loader-title { font-family: 'Manrope',sans-serif; font-size: 1.25rem; font-weight: 600; color: #10233F; margin-bottom: 12px; }
.loader-phrase { font-family: 'Inter',sans-serif; font-size: 0.95rem; color: #5D718B; margin-bottom: 16px; min-height: 1.5em; font-style: italic; }
.loader-hint { font-family: 'Inter',sans-serif; font-size: 0.82rem; color: #8091A7; line-height: 1.5; }
</style>
