// Client exports
export { apiClient, API_BASE_URL } from './client'
export { SESSION_EXPIRED_EVENT, emitSessionExpired } from './session'
export type {
  ApiResponseWrapper,
  RequestConfig,
  QueryParams,
  QueryParamValue,
} from './types'

// Error exports
export {
  ApiError,
  ErrorCode,
  ErrorType,
  isApiError,
  getErrorMessage,
  createNetworkError,
  toApiError,
  hasErrorType,
  hasErrorCode,
} from './errors'
export type { ErrorCodeType, ErrorTypeValue } from './errors'

// Query Key exports
export { queryKeys } from './queryKeys'
export type { QueryKeyOf, AuthQueryKey } from './queryKeys'
