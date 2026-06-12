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
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useI18n } from 'vue-i18n'

// PROTOTYPE MOCK ONLY — THESE PHRASES SIMULATE LONG-RUNNING AI/PDF PROCESSING UX.
// REPLACE PROCESS STATE WITH REAL BACKEND STATUS EVENTS OR API POLLING LATER.

const EN_PHRASES = [
  'Reticulating splines...',
  'Assembling the Avengers...',
  'Bending time and space...',
  'Following the white rabbit... (soon will escape The Matrix)',
  'Winter is loading...',
  'Buffing the hamsters... (for them to spin wheels faster)',
  'Marinating data (letting them to infuse flavor)',
  'Shoveling coal into the server (the old train still running)',
  'Noodling with code... (the little cook trying hard)',
  'Baking cookies... (Grandma\'s recipe for cache)',
  'Ctrl+Z in progress... (hoping nobody noticed)',
  'Reading your profile and admiring...',
  'Learning to write like Shakespeare...',
  'Server is tired, left a note "Went for coffee... Be right back"',
  'Get stuck in Reels, TikToks and Shorts... Recommend you the same while waiting...',
  'Chillax... (while we are working)',
  'Starting nuclear reactors to power up data centers with AI model for you...',
  'Hunting for more RAM memory to run best AI models'
]

const RU_PHRASES = [
  'Натягиваем сову на глобус',
  'Стучимся в чат к Мстителям',
  'Включаем гипердрайв',
  'Следуем за белым кроликом (скоро выйдем из Матрицы)',
  'Зима близко...',
  'Кормим серверных хомяков (чтобы крутили колесики быстрее)',
  'Маринуем данные (пусть настоятся для вкуса)',
  'Подкидываем уголька в сервер (старый паровоз еще дышит)',
  'Лепим код-спагетти... (поваренок очень старается)',
  'Выпекаем куки... (Бабушкин рецепт кэша)',
  'Cудорожно жмем Ctrl+Z... (надеемся, никто не заметил)',
  'Читаем твой профиль и восхищаемся им...',
  'Учимся писать как Пушкин...',
  'Сервер устал, оставил записку "Ушел за кофе... Скоро буду"',
  'Залип в рилсах, тиктоках и шортсах... Советую и тебе пока ждёшь...',
  'Узбагойся... (пока мы работаем)',
  'Запускаем ядерные реакторы для питания дата центров с ИИ моделью для тебя...',
  'Гоняемся за большим количеством оперативной памяти для запуска лучших ИИ моделей...'
]

const props = defineProps<{
  title: string
  hint: string
}>()

const { locale } = useI18n()
const currentPhrase = ref('')
let intervalId: ReturnType<typeof setInterval> | null = null
let lastIndex = -1

function pickRandomPhrase(): string {
  const phrases = locale.value === 'ru' ? RU_PHRASES : EN_PHRASES
  let idx: number
  do {
    idx = Math.floor(Math.random() * phrases.length)
  } while (idx === lastIndex && phrases.length > 1)
  lastIndex = idx
  return phrases[idx]
}

function rotatePhrase() {
  currentPhrase.value = pickRandomPhrase()
}

onMounted(() => {
  rotatePhrase()
  // PROTOTYPE MOCK ONLY — ROTATE PHRASES EVERY 3 SECONDS. REPLACE WITH REAL STATUS POLLING.
  intervalId = setInterval(rotatePhrase, 3000)
})

onBeforeUnmount(() => {
  if (intervalId) {
    clearInterval(intervalId)
    intervalId = null
  }
})
</script>

<style scoped>
.loader-overlay {
  position: fixed;
  inset: 0;
  background: rgba(22, 33, 43, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}
.loader-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 32px;
  max-width: 480px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(16, 35, 63, 0.14);
}
.loader-spinner {
  margin-bottom: 20px;
}
.loader-title {
  font-family: 'Manrope', sans-serif;
  font-size: 1.25rem;
  font-weight: 600;
  color: #10233F;
  margin-bottom: 12px;
}
.loader-phrase {
  font-family: 'Inter', sans-serif;
  font-size: 0.95rem;
  color: #5D718B;
  margin-bottom: 16px;
  min-height: 1.5em;
  font-style: italic;
  transition: opacity 0.3s;
}
.loader-hint {
  font-family: 'Inter', sans-serif;
  font-size: 0.82rem;
  color: #8091A7;
  line-height: 1.5;
}
</style>
