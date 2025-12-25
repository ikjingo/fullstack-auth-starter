/**
 * 테스트 렌더링 헬퍼
 *
 * Provider들이 래핑된 커스텀 render 함수를 제공합니다.
 */
import React from 'react'
import { render, type RenderOptions } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'

/**
 * 테스트용 QueryClient 생성
 * - 재시도 비활성화
 * - 에러 로깅 비활성화
 */
export function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  })
}

interface WrapperProps {
  children: React.ReactNode
}

/**
 * 테스트용 Provider 래퍼 생성
 */
export function createWrapper(queryClient?: QueryClient) {
  const client = queryClient ?? createTestQueryClient()

  return function Wrapper({ children }: WrapperProps) {
    return (
      <QueryClientProvider client={client}>
        <BrowserRouter>
          {children}
        </BrowserRouter>
      </QueryClientProvider>
    )
  }
}

interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  queryClient?: QueryClient
  route?: string
}

/**
 * Provider들이 래핑된 커스텀 render 함수
 *
 * @example
 * const { getByText } = renderWithProviders(<MyComponent />)
 *
 * @example
 * // 커스텀 QueryClient 사용
 * const queryClient = createTestQueryClient()
 * renderWithProviders(<MyComponent />, { queryClient })
 */
export function renderWithProviders(
  ui: React.ReactElement,
  options: CustomRenderOptions = {}
) {
  const { queryClient, route = '/', ...renderOptions } = options

  // 라우트 설정
  window.history.pushState({}, 'Test page', route)

  const Wrapper = createWrapper(queryClient)

  return {
    ...render(ui, { wrapper: Wrapper, ...renderOptions }),
    queryClient: queryClient ?? createTestQueryClient(),
  }
}

/**
 * Provider 없이 순수 컴포넌트 렌더링
 * UI 컴포넌트 테스트용
 */
export { render as renderPure } from '@testing-library/react'
