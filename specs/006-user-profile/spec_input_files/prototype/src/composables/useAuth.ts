import { ref } from 'vue'
import * as authService from '@/services/authService'

const role = ref<string | null>('USER')
const authenticated = ref(true)

export function useAuth() {
  async function logout() {
    await authService.logout()
    role.value = null
    authenticated.value = false
  }

  return { role, authenticated, logout }
}
