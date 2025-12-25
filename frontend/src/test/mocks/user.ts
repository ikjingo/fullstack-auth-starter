/**
 * 사용자 목 데이터 팩토리
 */
import type { User } from '@/types'

let userIdCounter = 1

/**
 * 기본 사용자 데이터 생성
 */
export function createMockUser(overrides?: Partial<User>): User {
  const id = userIdCounter++
  return {
    id: `user-${id}`,
    email: `user${id}@example.com`,
    nickname: `테스트유저${id}`,
    role: 'USER',
    ...overrides,
  }
}

/**
 * 관리자 사용자 데이터 생성
 */
export function createMockAdminUser(overrides?: Partial<User>): User {
  return createMockUser({
    role: 'ADMIN',
    ...overrides,
  })
}

/**
 * 카운터 리셋 (테스트 간 격리용)
 */
export function resetUserIdCounter(): void {
  userIdCounter = 1
}
