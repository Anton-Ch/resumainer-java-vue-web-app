/**
 * Shared HTTP client with CSRF token handling (OWASP cookie-to-header pattern).
 *
 * Reads XSRF-TOKEN from the non-HTTP-only cookie set by CsrfFilter,
 * and sends it as X-CSRF-Token header for unsafe methods (POST, PUT, PATCH, DELETE).
 *
 * All requests include credentials (session cookie) for authentication.
 */

function getCsrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : ''
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

  // Add CSRF token for unsafe methods
  if (isUnsafeMethod(method)) {
    const csrfToken = getCsrfToken()
    if (csrfToken) {
      headers.set('X-CSRF-Token', csrfToken)
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
