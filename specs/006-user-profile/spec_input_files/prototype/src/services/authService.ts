export interface AuthStatus {
  authenticated: boolean
  role: string | null
}

const mockAuth: AuthStatus = { authenticated: true, role: 'USER' }

export async function checkAuthStatus(): Promise<AuthStatus> {
  return mockAuth
}

export async function login(email: string, password: string): Promise<void> {
  mockAuth.authenticated = true
  mockAuth.role = 'USER'
}

export async function register(email: string, password: string): Promise<void> {
  mockAuth.authenticated = true
  mockAuth.role = 'USER'
}

export async function logout(): Promise<void> {
  mockAuth.authenticated = false
  mockAuth.role = null
}
