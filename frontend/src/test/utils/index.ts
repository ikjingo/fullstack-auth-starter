/**
 * 테스트 유틸리티 모듈
 *
 * 테스트에서 공통으로 사용되는 유틸리티 함수들을 제공합니다.
 *
 * @example
 * import { renderWithProviders, createTestQueryClient } from '@/test/utils'
 *
 * describe('MyComponent', () => {
 *   it('renders correctly', () => {
 *     const { getByText } = renderWithProviders(<MyComponent />)
 *     expect(getByText('Hello')).toBeInTheDocument()
 *   })
 * })
 */

// Render helpers
export {
  renderWithProviders,
  renderPure,
  createTestQueryClient,
  createWrapper,
} from './render-helpers'

// Store helpers
export {
  createMockUserStore,
  createMockToastStore,
  createMockThemeStore,
  mockUserStoreWith,
} from './store-helpers'

// Re-export testing library utilities for convenience
export { screen, waitFor, within, fireEvent } from '@testing-library/react'
export { default as userEvent } from '@testing-library/user-event'
