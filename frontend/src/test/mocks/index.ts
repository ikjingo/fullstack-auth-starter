/**
 * 목 데이터 팩토리 모듈
 *
 * 테스트에서 일관된 목 데이터를 생성하기 위한 팩토리 함수들을 제공합니다.
 *
 * @example
 * import { createMockUser, createMockLoginResponse } from '@/test/mocks'
 *
 * const user = createMockUser({ role: 'ADMIN' })
 * const response = createMockLoginResponse()
 */

// User mocks
export {
  createMockUser,
  createMockAdminUser,
  resetUserIdCounter,
} from './user'

// API response mocks
export {
  createMockLoginResponse,
  createMockApiErrorResponse,
} from './api-responses'
