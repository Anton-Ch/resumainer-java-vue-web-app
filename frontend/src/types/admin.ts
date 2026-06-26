/**
 * TypeScript interfaces for Admin Console feature.
 * Matches backend DTOs exactly.
 *
 * Critical naming conventions:
 * - PagedResponse uses items (not content)
 * - isCurrentAdmin and isPrivileged keep their exact names
 * - accountEmail and resumeEmail are separate fields
 */

export interface PagedResponse<T> {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AdminDashboard {
  totalUsers: number
  totalResumes: number
  totalTokensSent: number
  totalTokensSentWip: boolean
  totalTokensGenerated: number
  totalTokensGeneratedWip: boolean
}

export interface AdminSavedResume {
  id: number
  ownerUserId: string
  ownerUsername: string
  ownerEmail: string
  ownerFullName: string | null
  resumeTitle: string
  vacancyTitle: string
  companyName: string
  languageCode: string
  languageName: string
  adaptationLevel: string
  createdAt: string
  publicUrlLink: string | null
  pdfOpenUrl: string
  pdfDownloadUrl: string
  htmlDownloadUrl: string | null
  pdfAvailable: boolean
  pdfStatus: string | null
  pdfMessage: string | null
  coverLetter: string | null
}

export interface AdminUserListItem {
  id: string
  fullName: string | null
  username: string
  email: string
  roleCode: string
  roleName: string
  statusCode: string
  statusName: string
  permissionCode: string
  permissionName: string
  isPrivileged: boolean
  resumesCount: number
  createdAt: string
}

export interface AdminUserAccount {
  id: string
  username: string
  accountEmail: string
  roleCode: string
  roleName: string
  statusCode: string
  statusName: string
  permissionCode: string
  permissionName: string
  isPrivileged: boolean
  defaultLanguageCode: string | null
  defaultLanguageName: string | null
  secondaryLanguageCode: string | null
  secondaryLanguageName: string | null
  createdAt: string | null
  updatedAt: string | null
}

export interface AdminUserContacts {
  fullName: string | null
  professionalTitle: string | null
  phone: string | null
  resumeEmail: string | null
  location: string | null
  linkedinUrl: string | null
  portfolioUrl: string | null
  telegram: string | null
  whatsapp: string | null
}

export interface AdminUserAdditionalInfo {
  skills: string | null
  languages: string | null
  professionalAspirations: string | null
  achievements: string | null
  generalInformation: string | null
  readyForRelocation: string | null
  readyForBusinessTrips: string | null
  dateOfBirth: string | null
  citizenship: string | null
}

export interface AdminUserDetails {
  id: string
  isCurrentAdmin: boolean
  account: AdminUserAccount
  contacts: AdminUserContacts | null
  additionalInfo: AdminUserAdditionalInfo | null
}

export interface AdminUserAccessUpdateRequest {
  roleCode: string
  statusCode: string
  permissionCode: string
  isPrivileged: boolean
}

/**
 * Admin resume listing query parameters.
 */
export interface AdminResumesQuery {
  page?: number
  size?: number
  search?: string
  language?: string
  adaptationLevel?: string
  createdFrom?: string
  createdTo?: string
  sort?: string
}

/**
 * Admin users listing query parameters.
 */
export interface AdminUsersQuery {
  page?: number
  size?: number
  search?: string
  role?: string
  status?: string
  permission?: string
  rights?: string
  createdFrom?: string
  createdTo?: string
  sort?: string
}
