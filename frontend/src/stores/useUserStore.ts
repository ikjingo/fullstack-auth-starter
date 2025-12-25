import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { tokenManager } from '@/api/client/tokenManager'

type UserRole = 'USER' | 'ADMIN'

interface User {
  id: string
  email: string
  nickname: string
  role: UserRole
  profileImageUrl?: string | null
  hasPassword: boolean
}

// Helper function to check if user is admin
export const isAdmin = (user: User | null): boolean => {
  return user?.role === 'ADMIN'
}

interface UserState {
  user: User | null
  isAuthenticated: boolean
  isInitializing: boolean
  setUser: (user: User | null) => void
  login: (user: User, token: string, refreshToken?: string, rememberMe?: boolean) => void
  logout: () => void
  setInitializing: (isInitializing: boolean) => void
}

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isInitializing: true,
      setUser: (user) => set({ user, isAuthenticated: !!user }),
      login: (user, token, refreshToken, rememberMe = false) => {
        // rememberMe가 true일 때만 refreshToken을 localStorage에 저장
        if (rememberMe && refreshToken) {
          tokenManager.setTokens(token, refreshToken)
        } else {
          tokenManager.setTokens(token)
        }
        set({ user, isAuthenticated: true })
      },
      logout: () => {
        // 메모리에서 토큰 삭제
        tokenManager.clearTokens()
        // 레거시 localStorage 토큰 정리 (마이그레이션용)
        localStorage.removeItem('auth-starter-token')
        localStorage.removeItem('auth-starter-refresh-token')
        set({ user: null, isAuthenticated: false })
      },
      setInitializing: (isInitializing) => set({ isInitializing }),
    }),
    {
      name: 'auth-starter-user',
      // 토큰은 persist하지 않음 (메모리에만 저장)
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
