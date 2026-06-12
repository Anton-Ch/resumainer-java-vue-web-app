import { createI18n } from 'vue-i18n'
import en from './en.json'
import ru from './ru.json'

const savedLocale = localStorage.getItem('locale') || 'en'

const i18n = createI18n({
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: {
    en,
    ru
  }
})

export default i18n
