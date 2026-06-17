/**
 * Global Vitest setup.
 * Installs PrimeVue plugin so tests don't crash with "$primevue not defined".
 */
import { config } from '@vue/test-utils'
import PrimeVue from 'primevue/config'

config.global.plugins = [PrimeVue]
