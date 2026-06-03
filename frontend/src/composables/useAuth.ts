import { ref, computed } from 'vue'
import {
  login as apiLogin,
  register as apiRegister,
  logout as apiLogout,
  checkAuthStatus,
  type AuthResponseData
} from '@/services/authService'

// ─── State (module-level, shared across all consumers) ──

/** Whether the current user is authenticated. */
const isAuthenticated = ref(false)

/** Current user's email. */
const email = ref('')

/** Current user's role: "USER" or "ADMIN". */
const role = ref('')

/** Whether an auth check is in progress. */
const loading = ref(false)

/** Last error message from login/register. */
const error = ref<string | null>(null)

/** Last success message from login/register. */
const successMessage = ref<string | null>(null)

// ─── Composable ─────────────────────────────────────────

/**
 * Auth composable.
 *
 * Provides reactive auth state and methods for login, register, logout.
 * State is shared (module-level refs) so it's consistent across all components.
 */
export function useAuth() {
  // ─── Computed ───────────────────────────────────────

  const user = computed(() => ({
    email: email.value,
    role: role.value
  }))

  // ─── Methods ────────────────────────────────────────

  /**
   * Check authentication status by calling /api/auth/status.
   * Returns the auth status for callers that need it (e.g., router guard).
   */
  async function checkAuth(): Promise<{ authenticated: boolean; email: string; role: string }> {
    loading.value = true
    try {
      const status = await checkAuthStatus()
      isAuthenticated.value = status.authenticated
      email.value = status.authenticated ? status.email : ''
      role.value = status.authenticated ? status.role : ''
      return status
    } catch {
      isAuthenticated.value = false
      email.value = ''
      role.value = ''
      return { authenticated: false, email: '', role: '' }
    } finally {
      loading.value = false
    }
  }

  /**
   * Log in with email and password.
   * On success, updates state and returns the response.
   */
  async function login(email_: string, password: string, rememberMe: boolean): Promise<AuthResponseData> {
    loading.value = true
    error.value = null
    successMessage.value = null
    try {
      const response = await apiLogin(email_, password, rememberMe)
      if (response.success) {
        isAuthenticated.value = true
        email.value = email_
        role.value = response.role || 'USER'
      }
      return response
    } catch (err: any) {
      const msg = err?.message || 'Login failed'
      error.value = msg
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Register a new user.
   * On success, updates state and returns the response.
   */
  async function register(
    email_: string,
    password: string,
    passwordConfirmation: string
  ): Promise<AuthResponseData> {
    loading.value = true
    error.value = null
    successMessage.value = null
    try {
      const response = await apiRegister(email_, password, passwordConfirmation)
      if (response.success) {
        isAuthenticated.value = true
        email.value = email_
        role.value = response.role || 'USER'
        successMessage.value = response.message || 'Account created successfully'
      }
      return response
    } catch (err: any) {
      const msg = err?.message || 'Registration failed'
      error.value = msg
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Log out. On success, clears auth state.
   */
  async function logout(): Promise<void> {
    loading.value = true
    try {
      await apiLogout()
    } finally {
      isAuthenticated.value = false
      email.value = ''
      role.value = ''
      loading.value = false
    }
  }

  /**
   * Clear error message.
   */
  function clearError(): void {
    error.value = null
  }

  // ─── Return ─────────────────────────────────────────

  return {
    // State
    isAuthenticated,
    email,
    role,
    user,
    loading,
    error,
    successMessage,

    // Methods
    checkAuth,
    login,
    register,
    logout,
    clearError
  }
}
