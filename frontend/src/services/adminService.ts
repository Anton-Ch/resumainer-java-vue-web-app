import { apiRequest } from '@/services/httpClient'
import type {
  AdminDashboard,
  AdminSavedResume,
  AdminUserListItem,
  AdminUserDetails,
  AdminUserAccessUpdateRequest,
  PagedResponse,
  AdminResumesQuery,
  AdminUsersQuery
} from '@/types/admin'

/**
 * API client for /api/admin/** endpoints.
 * Every endpoint is protected by AuthInterceptor ADMIN role check on the backend.
 */

const BASE = '/api/admin'

function buildQueryString(params: Record<string, string | number | boolean | undefined | null>): string {
  const parts: string[] = []
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    }
  }
  return parts.length > 0 ? '?' + parts.join('&') : ''
}

/**
 * Fetch admin dashboard stats.
 */
export async function getAdminDashboard(): Promise<AdminDashboard> {
  return apiRequest<AdminDashboard>(`${BASE}/dashboard`)
}

/**
 * Fetch paginated admin resumes listing.
 */
export async function getAdminResumes(query: AdminResumesQuery = {}): Promise<PagedResponse<AdminSavedResume>> {
  const qs = buildQueryString({
    page: query.page,
    size: query.size,
    search: query.search,
    language: query.language,
    adaptationLevel: query.adaptationLevel,
    createdFrom: query.createdFrom,
    createdTo: query.createdTo,
    sort: query.sort
  })
  return apiRequest<PagedResponse<AdminSavedResume>>(`${BASE}/resumes${qs}`)
}

/**
 * Delete a resume as admin (soft-delete).
 */
export async function deleteAdminResume(resumeId: number): Promise<{ message: string }> {
  return apiRequest<{ message: string }>(`${BASE}/resumes/${resumeId}`, { method: 'DELETE' })
}

/**
 * Fetch paginated admin users listing.
 */
export async function getAdminUsers(query: AdminUsersQuery = {}): Promise<PagedResponse<AdminUserListItem>> {
  const qs = buildQueryString({
    page: query.page,
    size: query.size,
    search: query.search,
    role: query.role,
    status: query.status,
    permission: query.permission,
    rights: query.rights,
    createdFrom: query.createdFrom,
    createdTo: query.createdTo,
    sort: query.sort
  })
  return apiRequest<PagedResponse<AdminUserListItem>>(`${BASE}/users${qs}`)
}

/**
 * Fetch admin user details with account, contacts, and additional info sections.
 */
export async function getAdminUserDetails(userId: string): Promise<AdminUserDetails> {
  return apiRequest<AdminUserDetails>(`${BASE}/users/${userId}`)
}

/**
 * Update user access control fields.
 * Sends isPrivileged (not privileged) per backend JSON contract.
 */
export async function updateAdminUserAccess(userId: string, request: AdminUserAccessUpdateRequest): Promise<AdminUserDetails> {
  return apiRequest<AdminUserDetails>(`${BASE}/users/${userId}/access`, {
    method: 'PATCH',
    body: JSON.stringify(request)
  })
}

/**
 * Soft-delete a user as admin (self-delete rejected by backend).
 */
export async function deleteAdminUser(userId: string): Promise<{ message: string }> {
  return apiRequest<{ message: string }>(`${BASE}/users/${userId}`, { method: 'DELETE' })
}
