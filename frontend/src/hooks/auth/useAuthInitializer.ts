/**
 * 인증 초기화 훅
 *
 * 앱 시작 시 저장된 토큰으로 로그인 상태를 복원합니다.
 * - localStorage에서 Refresh Token 확인
 * - Refresh Token이 있으면 Access Token 갱신 시도
 * - 성공 시 로그인 상태 유지, 실패 시 로그아웃 처리
 */
import { useEffect } from 'react'
import { tokenManager } from '@/api/client/tokenManager'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/useUserStore'

/**
 * 인증 초기화 훅
 *
 * App.tsx에서 호출하여 페이지 로드 시 로그인 상태를 복원합니다.
 */
export function useAuthInitializer(): void {
  const { setUser, logout, setInitializing, isAuthenticated } = useUserStore()

  useEffect(() => {
    async function initializeAuth() {
      // 이미 초기화된 경우 스킵
      if (tokenManager.isInitialized()) {
        setInitializing(false)
        return
      }

      // Refresh Token이 없으면 바로 종료
      if (!tokenManager.hasRefreshToken()) {
        // 로컬 스토리지에 user 정보가 있지만 토큰이 없는 경우 정리
        if (isAuthenticated) {
          logout()
        }
        setInitializing(false)
        return
      }

      try {
        // 토큰 복원 시도
        const restored = await tokenManager.initializeFromStorage()

        if (restored) {
          // Access Token 갱신 성공 - 사용자 정보 조회
          const user = await authApi.getMe()
          setUser({
            id: String(user.id),
            email: user.email,
            nickname: user.nickname,
            role: user.role as 'USER' | 'ADMIN',
            profileImageUrl: user.profileImageUrl,
            hasPassword: user.hasPassword,
          })
        } else {
          // 토큰 갱신 실패 - 로그아웃 처리
          logout()
        }
      } catch {
        // 오류 발생 시 로그아웃 처리
        logout()
      } finally {
        setInitializing(false)
      }
    }

    initializeAuth()
  }, [setUser, logout, setInitializing, isAuthenticated])
}
