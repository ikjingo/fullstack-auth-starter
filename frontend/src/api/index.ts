// Client exports
export { apiClient, API_BASE_URL, SESSION_EXPIRED_EVENT, emitSessionExpired } from './client'
export { ApiError } from './client'
export type { ApiResponseWrapper, RequestConfig } from './client'

// Shared exports
export * from './shared'

// Service exports
export { authApi } from './auth'
export { profileApi } from './profile'
export { passwordApi } from './password'
