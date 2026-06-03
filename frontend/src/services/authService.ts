/**
 * Auth API service.
 *
 * Handles HTTP calls to /api/auth/* endpoints.
 * Reads CSRF token from cookie and sends it as X-CSRF-Token header
 * (OWASP cookie-to-header pattern via CsrfFilter).
 */

const API_BASE = '/api/auth'

/**
 * Response from /api/auth/register and /api/auth/login
 */
export interface AuthResponseData {
  success: boolean
  role?: string | null
  message?: string | null
  redirectUrl?: string | null
}

/**
 * Response from /api/auth/status
 */
export interface AuthStatusData {
  authenticated: boolean
  email: string
  role: string
}

// ─── Helpers ────────────────────────────────────────────

/**
 * Read XSRF-TOKEN from document.cookie.
 * The cookie is set by CsrfFilter (non-HTTP-only, so JS can read it).
 */
function getCsrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : ''
}

/**
 * Build fetch options with JSON body, content type, and CSRF header.
 */
function buildOptions(method: string, body?: unknown): RequestInit {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }

  const csrfToken = getCsrfToken()
  if (csrfToken) {
    headers['X-CSRF-Token'] = csrfToken
  }

  return {
    method,
    headers,
    credentials: 'include', // send cookies (session)
    body: body ? JSON.stringify(body) : undefined
  }
}

/**
 * Parse JSON response, throw on HTTP errors.
 */
async function handleResponse<T>(response: Response): Promise<T> {
  const data = await response.json()
  if (!response.ok) {
    throw data as T
  }
  return data as T
}

// ─── API Methods ────────────────────────────────────────

/**
 * Register a new user.
 */
export async function register(
  email: string,
  password: string,
  passwordConfirmation: string
): Promise<AuthResponseData> {
  const res = await fetch(
    `${API_BASE}/register`,
    buildOptions('POST', { email, password, passwordConfirmation })
  )
  return handleResponse<AuthResponseData>(res)
}

/**
 * Log in with email and password.
 */
export async function login(
  email: string,
  password: string,
  rememberMe: boolean
): Promise<AuthResponseData> {
  const res = await fetch(
    `${API_BASE}/login`,
    buildOptions('POST', { email, password, rememberMe })
  )
  return handleResponse<AuthResponseData>(res)
}

/**
 * Log out (invalidate session).
 */
export async function logout(): Promise<AuthResponseData> {
  const res = await fetch(
    `${API_BASE}/logout`,
    buildOptions('POST')
  )
  return handleResponse<AuthResponseData>(res)
}

/**
 * Check current authentication status.
 * Used by useAuth composable and router guard.
 */
export async function checkAuthStatus(): Promise<AuthStatusData> {
  const res = await fetch(
    `${API_BASE}/status`,
    { credentials: 'include', headers: { 'Accept': 'application/json' } }
  )
  return handleResponse<AuthStatusData>(res)
}
