/**
 * Shared HTTP client with CSRF token handling (Spring Security CookieCsrfTokenRepository).
 *
 * Bootstraps CSRF token via GET /api/csrf on first unsafe request.
 * Reads XSRF-TOKEN from the non-HTTP-only cookie set by Spring Security,
 * and sends it as X-XSRF-TOKEN header for unsafe methods (POST, PUT, PATCH, DELETE).
 *
 * All requests include credentials (session cookie) for authentication.
 */

let csrfBootstrapPromise: Promise<void> | null = null

/**
 * Read XSRF-TOKEN from document.cookie.
 * The cookie is set by Spring Security CookieCsrfTokenRepository (non-HTTP-only, so JS can read it).
 */
function getCsrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : ''
}

/**
 * Ensure CSRF token is bootstrapped by calling GET /api/csrf if no token cookie exists.
 * @param force if true, always re-bootstrap even if cookie exists
 */
export async function ensureCsrfToken(force = false): Promise<void> {
  if (!force && getCsrfToken()) return

  if (!csrfBootstrapPromise || force) {
    csrfBootstrapPromise = fetch('/api/csrf', {
      method: 'GET',
      credentials: 'include',
      headers: { Accept: 'application/json' }
    }).then(async response => {
      if (!response.ok) {
        throw new Error(`Failed to bootstrap CSRF token: ${response.status}`)
      }
    }).finally(() => {
      csrfBootstrapPromise = null
    })
  }

  await csrfBootstrapPromise
}

function isUnsafeMethod(method: string): boolean {
  const normalized = method.toUpperCase()
  return !['GET', 'HEAD', 'OPTIONS'].includes(normalized)
}

async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json()
    return body.message || body.error || `Request failed with status ${response.status}`
  } catch {
    return `Request failed with status ${response.status}`
  }
}

export async function apiRequest<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)

  // Set Content-Type for requests with body if not already set
  const method = (options.method || 'GET').toUpperCase()
  if (options.body !== undefined && options.body !== null && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  // Bootstrap CSRF token for unsafe methods, then add header
  if (isUnsafeMethod(method)) {
    await ensureCsrfToken()
    const csrfToken = getCsrfToken()
    if (csrfToken) {
      headers.set('X-XSRF-TOKEN', csrfToken)
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include'
  })

  if (!response.ok) {
    throw new Error(await parseErrorMessage(response))
  }

  // 204 No Content
  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}
