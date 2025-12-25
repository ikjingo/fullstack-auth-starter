/**
 * Zustand 스토어 테스트 헬퍼
 *
 * 스토어 상태를 목킹하고 리셋하기 위한 유틸리티를 제공합니다.
 */
import { vi } from 'vitest'
import type { User } from '@/types'

/**
 * useUserStore 목 생성
 */
export function createMockUserStore(overrides?: {
  user?: User | null
  isAuthenticated?: boolean
  isLoading?: boolean
}) {
  const mockStore = {
    user: overrides?.user ?? null,
    isAuthenticated: overrides?.isAuthenticated ?? false,
    isLoading: overrides?.isLoading ?? false,
    login: vi.fn(),
    logout: vi.fn(),
    setUser: vi.fn(),
    checkAuth: vi.fn(),
  }

  return mockStore
}

/**
 * useToastStore 목 생성
 */
export function createMockToastStore() {
  return {
    toasts: [],
    addToast: vi.fn(),
    removeToast: vi.fn(),
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn(),
  }
}

/**
 * useThemeStore 목 생성
 */
export function createMockThemeStore(overrides?: {
  theme?: 'light' | 'dark' | 'system'
  resolvedTheme?: 'light' | 'dark'
}) {
  return {
    theme: overrides?.theme ?? 'system',
    resolvedTheme: overrides?.resolvedTheme ?? 'light',
    setTheme: vi.fn(),
  }
}

/**
 * 스토어 모킹 유틸리티
 *
 * @example
 * vi.mock('@/stores/useUserStore', () => ({
 *   useUserStore: () => createMockUserStore({ user: mockUser })
 * }))
 */
export function mockUserStoreWith(user: User | null) {
  return {
    useUserStore: vi.fn(() => createMockUserStore({ user, isAuthenticated: !!user })),
  }
}
